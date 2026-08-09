package com.example.micblast

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Process
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.roundToInt

class AudioLoopbackService : Service() {

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var loopThread: Thread? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    @Volatile
    private var pitchShifter: PitchShifter? = null

    @Volatile
    private var ringModPhase = 0.0

    @Volatile
    private var currentMode = MODE_NORMAL

    private var audioSetup = SETUP_WIRED_TO_SPEAKER
    private var sampleRate = 44100

    // Digital loudness multiplier, 1.0x-2.0x (like VLC/MX Player's software
    // volume boost). The actual output level is left to the phone's own
    // hardware volume, controlled by the user via the volume keys.
    @Volatile
    private var gain = 1f

    @Volatile
    private var intensity = 0.5f

    @Volatile
    private var running = false

    // True from the moment startLoopback() is called until beginAudioPipeline()
    // finishes (or aborts). Covers the async Bluetooth SCO connect window,
    // where `running` is still false but a second ACTION_START would otherwise
    // re-register scoReceiver and crash with "Receiver already registered".
    @Volatile
    private var starting = false

    private var scoReceiverRegistered = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private var scoTimeoutRunnable: Runnable? = null

    private lateinit var audioManager: AudioManager

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                Log.d(TAG, "Audio focus lost ($focusChange) — stopping loopback")
                stopLoopback()
            }
        }
    }

    private val scoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val state = intent?.getIntExtra(
                AudioManager.EXTRA_SCO_AUDIO_STATE,
                AudioManager.SCO_AUDIO_STATE_ERROR
            )
            Log.d(TAG, "SCO state update: $state")
            if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
                scoTimeoutRunnable?.let { mainHandler.removeCallbacks(it) }
                beginAudioPipeline()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                currentMode = intent.getStringExtra(EXTRA_MODE) ?: MODE_NORMAL
                audioSetup = intent.getStringExtra(EXTRA_AUDIO_SETUP) ?: SETUP_WIRED_TO_SPEAKER
                gain = intent.getFloatExtra(EXTRA_GAIN, 1f).coerceIn(1f, 2f)
                intensity = intent.getFloatExtra(EXTRA_INTENSITY, 0.5f)
                startLoopback()
            }
            ACTION_STOP -> stopLoopback()
            ACTION_CHANGE_MODE -> {
                val mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_NORMAL
                changeMode(mode)
                updateNotification()
            }
            ACTION_SET_GAIN -> {
                gain = intent.getFloatExtra(EXTRA_GAIN, 1f).coerceIn(1f, 2f)
                Log.d(TAG, "Gain set to $gain")
                updateNotification()
            }
            ACTION_SET_INTENSITY -> {
                intensity = intent.getFloatExtra(EXTRA_INTENSITY, 0.5f).coerceIn(0f, 1f)
                Log.d(TAG, "Intensity set to $intensity")
                updateNotification()
            }
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(TAG, "Task removed — force stopping loopback")
        stopLoopback()
        super.onTaskRemoved(rootIntent)
    }

    private fun deviceTypeName(type: Int): String = when (type) {
        AudioDeviceInfo.TYPE_WIRED_HEADSET -> "WIRED_HEADSET"
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "WIRED_HEADPHONES"
        AudioDeviceInfo.TYPE_BUILTIN_MIC -> "BUILTIN_MIC"
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "BLUETOOTH_SCO"
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "BLUETOOTH_A2DP"
        AudioDeviceInfo.TYPE_USB_DEVICE -> "USB_DEVICE"
        AudioDeviceInfo.TYPE_USB_HEADSET -> "USB_HEADSET"
        else -> "OTHER($type)"
    }

    private fun changeMode(mode: String) {
        currentMode = mode
        if (running) {
            pitchShifter = when (mode) {
                MODE_CHIPMUNK, MODE_DEEP, MODE_ROBOT -> PitchShifter(sampleRate)
                else -> null
            }
            ringModPhase = 0.0
            Log.d(TAG, "Mode changed live to $mode")
        }
    }

    private fun startLoopback() {
        if (running || starting) return
        starting = true

        val usesCommunicationRouting = audioSetup != SETUP_PHONE_MIC_TO_BT_SPEAKER
        val focusUsage = if (usesCommunicationRouting) {
            AudioAttributes.USAGE_VOICE_COMMUNICATION
        } else {
            AudioAttributes.USAGE_MEDIA
        }
        val focusContentType = if (usesCommunicationRouting) {
            AudioAttributes.CONTENT_TYPE_SPEECH
        } else {
            AudioAttributes.CONTENT_TYPE_MUSIC
        }

        val focusResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(focusUsage)
                        .setContentType(focusContentType)
                        .build()
                )
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build()
            audioFocusRequest = request
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                focusChangeListener,
                if (usesCommunicationRouting) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }

        if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(TAG, "Audio focus not granted, not starting")
            starting = false
            stopSelf()
            broadcastState(false)
            return
        }

        // ServiceCompat (not the raw two-arg startForeground) so this
        // declares FOREGROUND_SERVICE_TYPE_MICROPHONE on API 29+ — required
        // at targetSdk 34 to match the manifest's foregroundServiceType and
        // avoid a MissingForegroundServiceTypeException — while still
        // falling back cleanly to the plain call on API < 29.
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        )

        if (audioSetup == SETUP_BT_MIC_TO_SPEAKER) {
            // Bluetooth mic capture only works over the SCO (call-audio)
            // link, not the regular streaming link — so we have to ask
            // Android to open that link first, then wait for it to
            // actually connect before touching AudioRecord.
            val filter = IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
            ContextCompat.registerReceiver(
                this, scoReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED
            )
            scoReceiverRegistered = true

            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.startBluetoothSco()
            audioManager.isBluetoothScoOn = true

            val timeout = Runnable {
                Log.d(TAG, "SCO connect timed out, proceeding anyway")
                beginAudioPipeline()
            }
            scoTimeoutRunnable = timeout
            mainHandler.postDelayed(timeout, 4000)
        } else {
            beginAudioPipeline()
        }
    }

    private fun beginAudioPipeline() {
        if (running) {
            starting = false
            return
        }

        sampleRate = 44100

        val minRecBuf = AudioRecord.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        val minPlayBuf = AudioTrack.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        // getMinBufferSize can return ERROR (-1) or ERROR_BAD_VALUE (-2) on
        // devices/configs that don't support this format. Doubling a
        // negative value stays negative and crashes the AudioRecord/
        // AudioTrack constructors below, so bail out cleanly instead.
        if (minRecBuf <= 0 || minPlayBuf <= 0) {
            Log.e(TAG, "Unsupported audio config: minRecBuf=$minRecBuf minPlayBuf=$minPlayBuf")
            abortStartup()
            return
        }
        val recBufSize = minRecBuf * 2
        val playBufSize = minPlayBuf * 2

        val chosenSource = when (audioSetup) {
            SETUP_BT_MIC_TO_SPEAKER -> MediaRecorder.AudioSource.VOICE_COMMUNICATION
            else -> {
                val unprocessedSupported = "true" == audioManager.getProperty(
                    AudioManager.PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED
                )
                if (unprocessedSupported) {
                    MediaRecorder.AudioSource.UNPROCESSED
                } else {
                    MediaRecorder.AudioSource.VOICE_RECOGNITION
                }
            }
        }
        Log.d(TAG, "audioSetup=$audioSetup, chosenSource=$chosenSource, mode=$currentMode")

        audioRecord = try {
            AudioRecord(
                chosenSource,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                recBufSize
            )
        } catch (e: Exception) {
            Log.e(TAG, "AudioRecord construction failed", e)
            null
        }
        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord failed to initialize (mic busy or unsupported config)")
            audioRecord?.release()
            audioRecord = null
            abortStartup()
            return
        }

        val inputDevices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
        inputDevices.forEach {
            Log.d(TAG, "Input device: ${deviceTypeName(it.type)} id=${it.id}")
        }

        val preferredType = when (audioSetup) {
            SETUP_WIRED_TO_SPEAKER -> AudioDeviceInfo.TYPE_WIRED_HEADSET
            SETUP_BT_MIC_TO_SPEAKER -> AudioDeviceInfo.TYPE_BLUETOOTH_SCO
            else -> AudioDeviceInfo.TYPE_BUILTIN_MIC
        }
        val preferredDevice = inputDevices.firstOrNull { it.type == preferredType }
        if (preferredDevice != null) {
            val success = audioRecord?.setPreferredDevice(preferredDevice)
            Log.d(TAG, "Forced input to ${deviceTypeName(preferredType)}, success=$success")
        } else {
            Log.d(TAG, "No ${deviceTypeName(preferredType)} input found, using default routing")
        }

        val usesCommunicationRouting = audioSetup != SETUP_PHONE_MIC_TO_BT_SPEAKER
        val trackUsage = if (usesCommunicationRouting) {
            AudioAttributes.USAGE_VOICE_COMMUNICATION
        } else {
            AudioAttributes.USAGE_MEDIA
        }
        val trackContentType = if (usesCommunicationRouting) {
            AudioAttributes.CONTENT_TYPE_SPEECH
        } else {
            AudioAttributes.CONTENT_TYPE_MUSIC
        }

        audioTrack = try {
            AudioTrack(
                AudioAttributes.Builder()
                    .setUsage(trackUsage)
                    .setContentType(trackContentType)
                    .build(),
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build(),
                playBufSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )
        } catch (e: Exception) {
            Log.e(TAG, "AudioTrack construction failed", e)
            null
        }
        if (audioTrack?.state != AudioTrack.STATE_INITIALIZED) {
            Log.e(TAG, "AudioTrack failed to initialize")
            audioRecord?.release()
            audioRecord = null
            audioTrack?.release()
            audioTrack = null
            abortStartup()
            return
        }
        // Left at unity on purpose — real output level is the phone's own
        // hardware volume now; `gain` below adds loudness on top of that.
        audioTrack?.setVolume(1f)

        if (audioSetup == SETUP_PHONE_MIC_TO_BT_SPEAKER) {
            // Don't just assume USAGE_MEDIA auto-routes to the connected
            // Bluetooth A2DP device — that's true on stock AOSP behavior,
            // but several OEM skins don't reliably route a freshly built
            // low-level AudioTrack the same way they'd route a MediaPlayer
            // session. Force it explicitly, the same way the mic input
            // above is forced to a specific device instead of hoping for
            // the best.
            val outputDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            outputDevices.forEach {
                Log.d(TAG, "Output device: ${deviceTypeName(it.type)} id=${it.id}")
            }
            val a2dpDevice = outputDevices.firstOrNull { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP }
            if (a2dpDevice != null) {
                val success = audioTrack?.setPreferredDevice(a2dpDevice)
                Log.d(TAG, "Forced output to BLUETOOTH_A2DP, success=$success")
            } else {
                Log.d(TAG, "No connected Bluetooth A2DP device found — falling back to default output routing")
            }
        }

        if (usesCommunicationRouting) {
            // Force playback out of the phone's own loudspeaker even
            // though a wired headset or Bluetooth SCO link is active —
            // both would otherwise try to claim the output too.
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isSpeakerphoneOn = true
        } else {
            // Phone mic -> Bluetooth speaker: plain media routing. The
            // explicit setPreferredDevice() above does the real work now;
            // this just makes sure nothing is holding the speakerphone or
            // in-call audio mode open from a previous session.
            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.isSpeakerphoneOn = false
        }


        pitchShifter = when (currentMode) {
            MODE_CHIPMUNK, MODE_DEEP, MODE_ROBOT -> PitchShifter(sampleRate)
            else -> null
        }
        ringModPhase = 0.0

        try {
            audioRecord?.startRecording()
            audioTrack?.play()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording/playback", e)
            audioRecord?.release()
            audioRecord = null
            audioTrack?.release()
            audioTrack = null
            abortStartup()
            return
        }
        running = true
        starting = false

        Log.d(
            TAG,
            "Recording started, routedDevice=${audioRecord?.routedDevice?.let { deviceTypeName(it.type) }}"
        )
        broadcastState(true)

        loopThread = Thread {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
            val buffer = ShortArray(recBufSize / 2)
            val processed = ShortArray(recBufSize / 2)
            try {
                while (running) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        applyVoiceEffect(buffer, read, processed)
                        applyGain(processed, read)
                        audioTrack?.write(processed, 0, read)
                    }
                }
            } catch (e: Exception) {
                // Something in the read/process/write path blew up mid-stream.
                // Without this, the thread would just die silently — the
                // notification would keep saying "live" and the UI would
                // keep showing Stop even though no audio is moving anymore.
                // Route the actual stop through the main thread so state
                // (notification, focus, UI broadcast) stays consistent.
                Log.e(TAG, "Audio loop crashed, stopping loopback", e)
                mainHandler.post { stopLoopback() }
            }
        }
        loopThread?.start()
    }

    // Cleans up whatever was already set up (foreground notification, audio
    // focus, SCO) when startup can't continue, so the service doesn't get
    // stuck showing a "live" notification for a session that never started.
    private fun abortStartup() {
        starting = false
        if (scoReceiverRegistered) {
            unregisterReceiver(scoReceiver)
            scoReceiverRegistered = false
        }
        if (audioManager.isBluetoothScoOn) {
            audioManager.isBluetoothScoOn = false
            audioManager.stopBluetoothSco()
        }
        resetAudioMode()
        releaseAudioFocus()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        broadcastState(false)
    }

    private fun applyVoiceEffect(input: ShortArray, len: Int, output: ShortArray) {
        when (currentMode) {
            MODE_CHIPMUNK -> {
                // 1.0 (no shift) at intensity 0, up to 2.2 (very squeaky) at 1.0
                val factor = 1.0f + intensity * 1.2f
                pitchShifter?.process(input, len, factor, output)
            }
            MODE_DEEP -> {
                // 1.0 (no shift) at intensity 0, down to 0.4 (very deep) at 1.0
                val factor = 1.0f - intensity * 0.6f
                pitchShifter?.process(input, len, factor, output)
            }
            MODE_ROBOT -> {
                // Pitch dips a little more as intensity rises, and the
                // buzzy ring-mod blends in more strongly too.
                val factor = 1.0f - intensity * 0.3f
                pitchShifter?.process(input, len, factor, output)
                val carrierFreq = 45.0
                val depth = intensity.toDouble()
                for (i in 0 until len) {
                    val mod = sin(2.0 * PI * carrierFreq * ringModPhase)
                    val mixed = output[i] * (1.0 - depth * 0.5 + depth * 0.5 * mod)
                    output[i] = mixed.toInt().coerceIn(-32768, 32767).toShort()
                    ringModPhase += 1.0 / sampleRate
                }
                if (ringModPhase > 1.0) ringModPhase %= 1.0
            }
            else -> {
                System.arraycopy(input, 0, output, 0, len)
            }
        }
    }

    // Digital loudness boost above the phone's normal max volume — the same
    // trick VLC/MX Player use for "software volume" beyond 100%. A tanh soft
    // clip is used instead of a hard clamp so the boosted peaks saturate
    // smoothly rather than crackling.
    private fun applyGain(samples: ShortArray, len: Int) {
        if (gain <= 1.0f) return
        for (i in 0 until len) {
            samples[i] = softClip(samples[i] * gain)
        }
    }

    private fun softClip(amplified: Float): Short {
        val normalized = (amplified / 32768f).coerceIn(-4f, 4f)
        val shaped = kotlin.math.tanh(normalized.toDouble()).toFloat()
        return (shaped * 32767f).toInt().coerceIn(-32768, 32767).toShort()
    }

    private fun stopLoopback() {
        starting = false
        scoTimeoutRunnable?.let { mainHandler.removeCallbacks(it) }
        scoTimeoutRunnable = null
        if (scoReceiverRegistered) {
            unregisterReceiver(scoReceiver)
            scoReceiverRegistered = false
        }
        if (audioManager.isBluetoothScoOn) {
            audioManager.isBluetoothScoOn = false
            audioManager.stopBluetoothSco()
        }

        if (!running && audioRecord == null && audioTrack == null) {
            releaseAudioFocus()
            resetAudioMode()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            broadcastState(false)
            return
        }

        running = false
        loopThread?.join(500)
        loopThread = null

        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null

        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null

        pitchShifter = null

        resetAudioMode()
        releaseAudioFocus()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        broadcastState(false)
    }

    private fun resetAudioMode() {
        audioManager.isSpeakerphoneOn = false
        audioManager.mode = AudioManager.MODE_NORMAL
    }

    private fun releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(focusChangeListener)
        }
    }

    private fun broadcastState(isRunning: Boolean) {
        val intent = Intent(ACTION_STATE_CHANGED).apply {
            setPackage(packageName)
            putExtra(EXTRA_RUNNING, isRunning)
        }
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        if (running) stopLoopback()
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, AudioLoopbackService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Tapping the notification body (not just the Stop action) brings
        // the app back to the foreground even after Home was pressed.
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val modeLabel = when (currentMode) {
            MODE_CHIPMUNK -> "Chipmunk"
            MODE_DEEP -> "Monster"
            MODE_ROBOT -> "Robot"
            else -> "Normal"
        }

        val setupLabel = when (audioSetup) {
            SETUP_BT_MIC_TO_SPEAKER -> "Bluetooth mic → phone speaker"
            SETUP_PHONE_MIC_TO_BT_SPEAKER -> "Phone mic → Bluetooth speaker"
            else -> "Wired mic → phone speaker"
        }

        val gainLabel = "Boost %.1f×".format(gain)
        val intensityLabel = if (currentMode == MODE_NORMAL) {
            "Intensity: N/A"
        } else {
            "Intensity: ${(intensity * 100f).roundToInt()}%"
        }

        val summary = "$modeLabel • $gainLabel"
        val details = "$setupLabel • $intensityLabel"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(summary)
            .setContentText(details)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$details\n$gainLabel • $modeLabel"))
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent)
            .build()
    }

    private fun updateNotification() {
        if (running || starting) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.notify(NOTIFICATION_ID, buildNotification())
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Audio Loopback", NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_START = "com.example.micblast.START"
        const val ACTION_STOP = "com.example.micblast.STOP"
        const val ACTION_CHANGE_MODE = "com.example.micblast.CHANGE_MODE"
        const val ACTION_SET_GAIN = "com.example.micblast.SET_GAIN"
        const val EXTRA_MODE = "com.example.micblast.MODE"
        const val EXTRA_GAIN = "com.example.micblast.GAIN"
        const val EXTRA_AUDIO_SETUP = "com.example.micblast.AUDIO_SETUP"
        const val ACTION_SET_INTENSITY = "com.example.micblast.SET_INTENSITY"
        const val EXTRA_INTENSITY = "com.example.micblast.INTENSITY"
        const val ACTION_STATE_CHANGED = "com.example.micblast.STATE_CHANGED"
        const val EXTRA_RUNNING = "com.example.micblast.RUNNING"

        const val MODE_NORMAL = "NORMAL"
        const val MODE_CHIPMUNK = "CHIPMUNK"
        const val MODE_DEEP = "DEEP"
        const val MODE_ROBOT = "ROBOT"

        const val SETUP_WIRED_TO_SPEAKER = "WIRED_TO_SPEAKER"
        const val SETUP_BT_MIC_TO_SPEAKER = "BT_MIC_TO_SPEAKER"
        const val SETUP_PHONE_MIC_TO_BT_SPEAKER = "PHONE_MIC_TO_BT_SPEAKER"

        const val CHANNEL_ID = "audio_loopback_channel"
        const val NOTIFICATION_ID = 1
        const val TAG = "MicBlast"
    }
}
