package com.example.micblast.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Returns a function that fires a short confirmation vibration pulse on tap,
 * or a no-op when the user has turned Haptic Feedback off in Settings.
 *
 * This talks to the device's Vibrator directly (needs the VIBRATE
 * permission, declared in the manifest) instead of going through Compose's
 * LocalHapticFeedback / View#performHapticFeedback. That path silently does
 * nothing on a lot of phones once the OS-level "Touch feedback" / "Vibrate
 * on tap" system setting is off - performHapticFeedback defers to that
 * setting unless you explicitly pass a flag to ignore it, which made this
 * app's own toggle feel broken/inert on those devices. Calling the Vibrator
 * directly always fires as long as this in-app toggle is on.
 */
@Composable
fun rememberHapticClick(enabled: Boolean): () -> Unit {
    val context = LocalContext.current
    val vibrator = remember(context) { context.systemVibrator() }
    return {
        if (enabled) {
            vibrator?.tick()
        }
    }
}

private fun Context.systemVibrator(): Vibrator? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

private fun Vibrator.tick() {
    if (!hasVibrator()) return
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
            vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
            vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
        else ->
            @Suppress("DEPRECATION")
            vibrate(25)
    }
}
