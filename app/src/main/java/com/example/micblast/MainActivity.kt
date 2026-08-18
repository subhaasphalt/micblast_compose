package com.example.micblast

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
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
import com.example.micblast.ui.ExitConfirmationDialog
import com.example.micblast.ui.MainScreen
import com.example.micblast.ui.SettingsScreen
import com.example.micblast.ui.WiredMicRequiredDialog
import com.example.micblast.ui.theme.AppThemes
import com.example.micblast.ui.theme.DefaultTheme
import com.example.micblast.ui.theme.MicBlastTheme
import com.example.micblast.ui.theme.themeById

class MainActivity : ComponentActivity() {

    // All screen state lives here as Compose state. MainScreen (and
    // everything under it) is a pure function of these values — it never
    // reaches into the service or Android APIs itself.
    private var isRunning by mutableStateOf(false)
    private var currentMode by mutableStateOf(AudioLoopbackService.MODE_REVERB)
    private var gainProgress by mutableIntStateOf(0) // 0-100 -> 1.0x-2.0x
    private var intensityProgress by mutableIntStateOf(50)
    private var audioSetupIndex by mutableIntStateOf(0)
    private var isLocked by mutableStateOf(false)
    private var showSettings by mutableStateOf(false)
    private var showExitConfirmation by mutableStateOf(false)
    private var showWiredMicRequired by mutableStateOf(false)
    private var wiredMicConnected by mutableStateOf(false)
    private var darkTheme by mutableStateOf(false)
    private var themeId by mutableStateOf(DefaultTheme.id)
    private var autoRotate by mutableStateOf(true)
    private var hapticsEnabled by mutableStateOf(true)

    private val settingsPrefs by lazy {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val audioManager by lazy {
        getSystemService(AudioManager::class.java)
    }

    // The wired-to-speaker setup feeds the phone's own speaker output right
    // back into whatever mic is capturing — harmless (and the whole point)
    // when a wired earphone's mic is doing the capturing, but an instant
    // screeching feedback loop if it falls back to the phone's built-in mic
    // instead. This tracks live whether a wired mic is actually plugged in,
    // so the UI can refuse to start (and explain why) rather than let that
    // happen.
    private val wiredDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
            refreshWiredMicConnected()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
            refreshWiredMicConnected()
        }
    }

    private fun refreshWiredMicConnected() {
        wiredMicConnected = audioManager
            ?.getDevices(AudioManager.GET_DEVICES_INPUTS)
            ?.any { it.type in AudioLoopbackService.WIRED_MIC_INPUT_TYPES }
            ?: false
    }

    // Audio setup order must match R.array.audio_setup_options.
    private val audioSetupValues = listOf(
        AudioLoopbackService.SETUP_WIRED_TO_SPEAKER,
        AudioLoopbackService.SETUP_PHONE_MIC_TO_BT_SPEAKER,
    )

    private var orientationEventListener: OrientationEventListener? = null

    // Keeps the UI in sync no matter how the service stops — the in-app
    // Stop button, the notification's Stop action, an incoming call taking
    // audio focus, the wired earphone being unplugged mid-session, or the
    // app being swiped out of recents.
    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            isRunning = intent?.getBooleanExtra(AudioLoopbackService.EXTRA_RUNNING, false) ?: false
            val stopReason = intent?.getStringExtra(AudioLoopbackService.EXTRA_STOP_REASON)
            if (stopReason == AudioLoopbackService.REASON_WIRED_DISCONNECTED) {
                refreshWiredMicConnected()
                Toast.makeText(
                    this@MainActivity,
                    "Earphone disconnected. Playback disabled",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Holds whichever of startLoopback()/restartWithNewSetup() triggered the
    // permission request currently in flight, so one launcher + one callback
    // can serve both call sites instead of duplicating the permission logic.
    private var pendingLoopbackAction: (() -> Unit)? = null

    // RequestMultiplePermissions instead of a single RECORD_AUDIO request so
    // POST_NOTIFICATIONS (needed to actually show the "live" notification on
    // API 33+) can be asked for in the same system dialog.
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        val micGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val action = pendingLoopbackAction
        pendingLoopbackAction = null

        when {
            !micGranted ->
                Toast.makeText(this, "Microphone permission is required", Toast.LENGTH_LONG).show()
            else -> action?.invoke()
        }
        // POST_NOTIFICATIONS is intentionally not checked here — if it's
        // denied, loopback still starts/continues fine, the persistent
        // "live" notification just won't be visible.
    }

    // Figures out which permissions are still missing to run loopback.
    // includeNotifications is left off for the "already running, just
    // switching setups" path — the notification is already showing at that
    // point, so there's nothing new to ask for there.
    private fun missingPermissionsFor(includeNotifications: Boolean): List<String> {
        val needed = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            needed += Manifest.permission.RECORD_AUDIO
        }

        if (includeNotifications &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            needed += Manifest.permission.POST_NOTIFICATIONS
        }

        return needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeId = settingsPrefs.getString(KEY_THEME_ID, DefaultTheme.id) ?: DefaultTheme.id
        // Resolve against the loaded theme's mode support immediately, so a
        // dark-only/light-only theme always renders (and reports its toggle
        // state) correctly even if the stored dark_theme value predates it
        // or was left over from a different theme.
        darkTheme = themeById(themeId).allowLightDark
            .resolveDarkTheme(settingsPrefs.getBoolean(KEY_DARK_THEME, false))
        autoRotate = settingsPrefs.getBoolean(KEY_AUTO_ROTATE, true)
        hapticsEnabled = settingsPrefs.getBoolean(KEY_HAPTICS, true)
        currentMode = settingsPrefs.getString(KEY_MODE, AudioLoopbackService.MODE_REVERB)
            ?: AudioLoopbackService.MODE_REVERB
        gainProgress = settingsPrefs.getInt(KEY_GAIN_PROGRESS, 0).coerceIn(0, 100)
        intensityProgress = settingsPrefs.getInt(KEY_INTENSITY_PROGRESS, 50).coerceIn(0, 100)
        audioSetupIndex = settingsPrefs.getInt(KEY_AUDIO_SETUP_INDEX, 0)
            .coerceIn(0, audioSetupValues.size - 1)
        setupOrientationLock()

        setContent {
            val activeTheme = themeById(themeId)

            MicBlastTheme(theme = activeTheme, darkTheme = darkTheme) {
                BackHandler(enabled = isLocked) {
                    // Locked — swallow back press instead of exiting/navigating,
                    // same as the old onBackPressed() override.
                }

                BackHandler(enabled = showSettings) {
                    showSettings = false
                }

                BackHandler(enabled = !isLocked && !showSettings) {
                    // Instead of exiting immediately, ask for confirmation so a
                    // stray/accidental back press doesn't silently kill the app
                    // (and any active loopback audio) mid-session.
                    showExitConfirmation = true
                }

                if (showSettings) {
                    SettingsScreen(
                        darkTheme = activeTheme.allowLightDark.resolveDarkTheme(darkTheme),
                        onDarkThemeChange = ::onDarkThemeChanged,
                        darkModeToggleEnabled = activeTheme.allowLightDark.allowsToggle,
                        themes = AppThemes,
                        selectedThemeId = activeTheme.id,
                        onThemeSelected = ::onThemeSelected,
                        autoRotate = autoRotate,
                        onAutoRotateChange = ::onAutoRotateChanged,
                        hapticFeedback = hapticsEnabled,
                        onHapticFeedbackChange = ::onHapticsChanged,
                        onBack = { showSettings = false },
                    )
                } else {
                    MainScreen(
                        isRunning = isRunning,
                        currentMode = currentMode,
                        gainProgress = gainProgress,
                        intensityProgress = intensityProgress,
                        audioSetupIndex = audioSetupIndex,
                        audioSetupLabels = stringArrayResource(R.array.audio_setup_options).toList(),
                        audioSetupCompactLabels = stringArrayResource(R.array.audio_setup_compact_options).toList(),
                        canPlay = selectedAudioSetup() != AudioLoopbackService.SETUP_WIRED_TO_SPEAKER || wiredMicConnected,
                        isLocked = isLocked,
                        hapticsEnabled = hapticsEnabled,
                        onPlayClick = ::onPlayRequested,
                        onStopClick = ::stopLoopback,
                        onModeSelect = ::selectMode,
                        onGainChange = ::onGainChanged,
                        onIntensityChange = ::onIntensityChanged,
                        onAudioSetupSelect = ::onAudioSetupSelected,
                        onLockClick = { isLocked = true },
                        onUnlock = { isLocked = false },
                        onMenuClick = { showSettings = true },
                    )
                }

                if (showWiredMicRequired) {
                    WiredMicRequiredDialog(
                        onDismiss = { showWiredMicRequired = false },
                    )
                }

                if (showExitConfirmation) {
                    ExitConfirmationDialog(
                        onConfirm = {
                            if (isRunning) stopLoopback()
                            showExitConfirmation = false
                            finish()
                        },
                        onDismiss = { showExitConfirmation = false },
                    )
                }
            }
        }
    }

    private fun onDarkThemeChanged(enabled: Boolean) {
        // Settings only shows this toggle as enabled when the active theme
        // supports both modes, but guard anyway since state can be stale
        // across a theme switch that happens the same frame.
        if (!themeById(themeId).allowLightDark.allowsToggle) return
        darkTheme = enabled
        settingsPrefs.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
    }

    private fun onThemeSelected(id: String) {
        val newTheme = themeById(id)
        themeId = newTheme.id
        // A dark-only/light-only theme forces the mode and greys out the
        // toggle; a both-modes theme keeps whatever the user last had set.
        darkTheme = newTheme.allowLightDark.resolveDarkTheme(darkTheme)
        settingsPrefs.edit()
            .putString(KEY_THEME_ID, themeId)
            .putBoolean(KEY_DARK_THEME, darkTheme)
            .apply()
    }

    private fun onAutoRotateChanged(enabled: Boolean) {
        autoRotate = enabled
        settingsPrefs.edit().putBoolean(KEY_AUTO_ROTATE, enabled).apply()
        if (enabled) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            if (orientationEventListener?.canDetectOrientation() == true) {
                orientationEventListener?.enable()
            }
        } else {
            orientationEventListener?.disable()
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun onHapticsChanged(enabled: Boolean) {
        hapticsEnabled = enabled
        settingsPrefs.edit().putBoolean(KEY_HAPTICS, enabled).apply()
    }

    private fun onPlayRequested() {
        if (selectedAudioSetup() == AudioLoopbackService.SETUP_WIRED_TO_SPEAKER && !wiredMicConnected) {
            showWiredMicRequired = true
            return
        }
        val needed = missingPermissionsFor(includeNotifications = true)
        if (needed.isEmpty()) {
            startLoopback()
        } else {
            pendingLoopbackAction = ::startLoopback
            requestPermissionsLauncher.launch(needed.toTypedArray())
        }
    }

    // Selecting a mode always updates which button is highlighted. If audio
    // is already running, it also tells the service to switch the live
    // effect instantly, with no stop/restart of the mic or speaker.
    private fun selectMode(mode: String) {
        currentMode = mode
        settingsPrefs.edit().putString(KEY_MODE, mode).apply()
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
        val newSetup = audioSetupValues.getOrElse(index) { AudioLoopbackService.SETUP_WIRED_TO_SPEAKER }
        if (isRunning && newSetup == AudioLoopbackService.SETUP_WIRED_TO_SPEAKER && !wiredMicConnected) {
            showWiredMicRequired = true
            return
        }
        audioSetupIndex = index
        settingsPrefs.edit().putInt(KEY_AUDIO_SETUP_INDEX, index).apply()
        if (isRunning) {
            val needed = missingPermissionsFor(includeNotifications = false)
            if (needed.isEmpty()) {
                restartWithNewSetup()
            } else {
                pendingLoopbackAction = ::restartWithNewSetup
                requestPermissionsLauncher.launch(needed.toTypedArray())
            }
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
    // the orientation directly from the raw sensor angle.
    //
    // Bug fix: previously requestedOrientation was left at its manifest
    // default (fullSensor) until the *first* sensor callback landed in the
    // portrait or reverse-portrait bucket. If the phone was already near
    // 90°/270° (the landscape dead zone, intentionally ignored below) at
    // that first callback — e.g. a cold start while mid-rotation — nothing
    // ever set requestedOrientation away from fullSensor, so the activity
    // could launch, or get stuck, sideways. Locking to PORTRAIT immediately
    // gives the dead zone a real "last known state" to hold onto instead of
    // falling through to fullSensor.
    private fun setupOrientationLock() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        orientationEventListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                when {
                    orientation in 0..29 || orientation in 331..360 ->
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    orientation in 151..209 ->
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                    // Angles near 90/270 would be landscape — this app never rotates to it.
                    // Whatever portrait/reverse-portrait state was last set (PORTRAIT, above,
                    // if nothing has fired yet) is simply held through this range.
                }
            }
        }

        // Only listen for rotation when the user has Auto Rotate on; otherwise
        // stay locked to plain portrait, matching the system's own Auto Rotate toggle.
        if (autoRotate && orientationEventListener?.canDetectOrientation() == true) {
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
        refreshWiredMicConnected()
        audioManager?.registerAudioDeviceCallback(wiredDeviceCallback, null)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(stateReceiver)
        audioManager?.unregisterAudioDeviceCallback(wiredDeviceCallback)
    }

    // Gain/intensity change continuously while dragging their sliders, so
    // they're persisted once here (on backgrounding) rather than on every
    // drag frame in onGainChanged/onIntensityChanged.
    override fun onPause() {
        super.onPause()
        settingsPrefs.edit()
            .putInt(KEY_GAIN_PROGRESS, gainProgress)
            .putInt(KEY_INTENSITY_PROGRESS, intensityProgress)
            .apply()
    }

    override fun onDestroy() {
        orientationEventListener?.disable()
        super.onDestroy()
    }

    private companion object {
        const val PREFS_NAME = "micblast_settings"
        const val KEY_DARK_THEME = "dark_theme"
        const val KEY_THEME_ID = "theme_id"
        const val KEY_AUTO_ROTATE = "auto_rotate"
        const val KEY_HAPTICS = "haptics_enabled"
        const val KEY_MODE = "last_mode"
        const val KEY_GAIN_PROGRESS = "last_gain_progress"
        const val KEY_INTENSITY_PROGRESS = "last_intensity_progress"
        const val KEY_AUDIO_SETUP_INDEX = "last_audio_setup_index"
    }
}
