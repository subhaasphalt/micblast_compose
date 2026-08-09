package com.example.micblast.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Returns a function that fires a short confirmation haptic pulse on tap,
 * or a no-op when the user has turned Haptic Feedback off in Settings.
 *
 * Uses View#performHapticFeedback under the hood (via Compose's
 * LocalHapticFeedback), which doesn't require the VIBRATE permission.
 */
@Composable
fun rememberHapticClick(enabled: Boolean): () -> Unit {
    val haptics = LocalHapticFeedback.current
    return {
        if (enabled) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}
