package com.example.micblast

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.OrientationEventListener
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringArrayResource
import androidx.core.content.ContextCompat
import com.example.micblast.ui.MainScreen
import com.example.micblast.ui.theme.MicBlastTheme

class MainActivity : ComponentActivity() {

    // All screen state lives here as Compose state. MainScreen (and
    // everything under it) is a pure function of these values — it never
    // reaches into the service or Android APIs itself.
    private var isRunning by mutableStateOf(false)
    private var currentMode by mutableStateOf(AudioLoopbackService.MODE_NORMAL)
    private var gainProgress by mutableIntStateOf(0) // 0-100 -> 1.0x-2.0x
    private var intensityProgress by mutableIntStateOf(50)
    private var audioSetupIndex by mutableIntStateOf(0)
    private var isLocked by mutableStateOf(false)

    // Audio setup order must match R.array.audio_setup_options.
    private val audioSetupValues = listOf(
        AudioLoopbackService.SETUP_WIRED_TO_SPEAKER,
        AudioLoopbackService.SETUP_BT_MIC_TO_SPEAKER,
        AudioLoopbackService.SETUP_PHONE_MIC_TO_BT_SPEAKER,
    )

    private var orientationEventListener: OrientationEventListener? = null

    // Keeps the UI in sync no matter how the service stops — the in-app
    // Stop button, the notification's Stop action, an incoming call taking
    // audio focus, or the app being swiped out of recents.
    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            isRunning = intent?.getBooleanExtra(AudioLoopbackService.EXTRA_RUNNING, false) ?: false
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            startLoopback()
        } else {
            Toast.makeText(this, "Microphone permission is required", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupOrientationLock()

        setContent {
            MicBlastTheme {
                BackHandler(enabled = isLocked) {
                    // Locked — swallow back press instead of exiting/navigating,
                    // same as the old onBackPressed() override.
                }

                MainScreen(
                    isRunning = isRunning,
                    currentMode = currentMode,
                    gainProgress = gainProgress,
                    intensityProgress = intensityProgress,
                    audioSetupIndex = audioSetupIndex,
                    audioSetupLabels = stringArrayResource(R.array.audio_setup_options).toList(),
                    isLocked = isLocked,
                    onPlayClick = ::onPlayRequested,
                    onStopClick = ::stopLoopback,
                    onModeSelect = ::selectMode,
                    onGainChange = ::onGainChanged,
                    onIntensityChange = ::onIntensityChanged,
                    onAudioSetupSelect = ::onAudioSetupSelected,
                    onLockClick = { isLocked = true },
                    onUnlock = { isLocked = false },
                    onMenuClick = {
                        // Settings screen (theme, haptics, RGB edge-lighting, etc.)
                        // is a separate piece of work — stubbed so the button
                        // isn't dead.
                        Toast.makeText(this, R.string.settings_coming_soon, Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }
    }

    private fun onPlayRequested() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startLoopback()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Selecting a mode always updates which button is highlighted. If audio
    // is already running, it also tells the service to switch the live
    // effect instantly, with no stop/restart of the mic or speaker.
    private fun selectMode(mode: String) {
        currentMode = mode
        if (isRunning) {
            val intent = Intent(this, AudioLoopbackService::class.java).apply {
                action = AudioLoopbackService.ACTION_CHANGE_MODE
                putExtra(AudioLoopbackService.EXTRA_MODE, mode)
            }
            startService(intent)
        }
    }

    private fun onGainChanged(progress: Int) {
        gainProgress = progress
        if (isRunning) {
            setServiceGain(gainForProgress(progress))
        }
    }

    private fun onIntensityChanged(progress: Int) {
        intensityProgress = progress
        if (isRunning) {
            setServiceIntensity(progress / 100f)
        }
    }

    // A different audio setup means a completely different mic/speaker
    // pipeline (wired vs Bluetooth SCO vs Bluetooth media) that can't be
    // hot-swapped like a voice effect can — so if audio is already playing,
    // this quietly stops and restarts the service with the new setup.
    // There's a brief (sub-second) gap in audio while the new pipeline
    // spins up, but no manual Stop/Play needed from the user.
    private fun onAudioSetupSelected(index: Int) {
        audioSetupIndex = index
        if (isRunning) {
            restartWithNewSetup()
        }
    }

    private fun selectedAudioSetup(): String =
        audioSetupValues.getOrElse(audioSetupIndex) { AudioLoopbackService.SETUP_WIRED_TO_SPEAKER }

    // Maps the 0-100 slider straight onto a 1.0x-2.0x digital gain, matching
    // the range VLC/MX Player expose for their "software volume boost". The
    // phone's hardware volume keys still control the actual output level;
    // this only adds loudness on top of that.
    private fun gainForProgress(progress: Int): Float = 1.0f + (progress / 100f)

    private fun startLoopback() {
        val intent = Intent(this, AudioLoopbackService::class.java).apply {
            action = AudioLoopbackService.ACTION_START
            putExtra(AudioLoopbackService.EXTRA_MODE, currentMode)
            putExtra(AudioLoopbackService.EXTRA_AUDIO_SETUP, selectedAudioSetup())
            putExtra(AudioLoopbackService.EXTRA_GAIN, gainForProgress(gainProgress))
            putExtra(AudioLoopbackService.EXTRA_INTENSITY, intensityProgress / 100f)
        }
        ContextCompat.startForegroundService(this, intent)
        isRunning = true
    }

    private fun stopLoopback() {
        val intent = Intent(this, AudioLoopbackService::class.java).apply {
            action = AudioLoopbackService.ACTION_STOP
        }
        startService(intent)
        isRunning = false
    }

    private fun restartWithNewSetup() {
        val stopIntent = Intent(this, AudioLoopbackService::class.java).apply {
            action = AudioLoopbackService.ACTION_STOP
        }
        startService(stopIntent)

        val startIntent = Intent(this, AudioLoopbackService::class.java).apply {
            action = AudioLoopbackService.ACTION_START
            putExtra(AudioLoopbackService.EXTRA_MODE, currentMode)
            putExtra(AudioLoopbackService.EXTRA_AUDIO_SETUP, selectedAudioSetup())
            putExtra(AudioLoopbackService.EXTRA_GAIN, gainForProgress(gainProgress))
            putExtra(AudioLoopbackService.EXTRA_INTENSITY, intensityProgress / 100f)
        }
        ContextCompat.startForegroundService(this, startIntent)
        // isRunning is already true here, so no visual state change needed —
        // Play/Stop correctly keeps showing Stop throughout.
    }

    private fun setServiceGain(gain: Float) {
        val intent = Intent(this, AudioLoopbackService::class.java).apply {
            action = AudioLoopbackService.ACTION_SET_GAIN
            putExtra(AudioLoopbackService.EXTRA_GAIN, gain)
        }
        startService(intent)
    }

    private fun setServiceIntensity(intensity: Float) {
        val intent = Intent(this, AudioLoopbackService::class.java).apply {
            action = AudioLoopbackService.ACTION_SET_INTENSITY
            putExtra(AudioLoopbackService.EXTRA_INTENSITY, intensity)
        }
        startService(intent)
    }

    // Restricts rotation to portrait and reverse-portrait ourselves. MIUI's
    // built-in "sensorPortrait" handling silently drops the 180° flip, so
    // instead the activity is left as fullSensor in the manifest and we pick
    // the orientation directly from the raw sensor angle. Always active —
    // there's no user-facing toggle for this anymore.
    private fun setupOrientationLock() {
        orientationEventListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                when {
                    orientation in 0..29 || orientation in 331..360 ->
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    orientation in 151..209 ->
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                    // Angles near 90/270 would be landscape — this app never rotates to it.
                }
            }
        }
        if (orientationEventListener?.canDetectOrientation() == true) {
            orientationEventListener?.enable()
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(AudioLoopbackService.ACTION_STATE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(stateReceiver, filter)
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(stateReceiver)
    }

    override fun onDestroy() {
        orientationEventListener?.disable()
        super.onDestroy()
    }
}
