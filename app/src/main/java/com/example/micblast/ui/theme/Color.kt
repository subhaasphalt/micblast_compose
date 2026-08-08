package com.example.micblast.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Raw palette values for dark mode (the app's original look). These mirror
 * the hex values MainScreen used to hardcode directly, now centralized here
 * so MicBlastTheme can actually drive them.
 */
object NeonPalette {
    val BgTop = Color(0xFF0B0F1A)
    val BgMid = Color(0xFF12101F)
    val BgBottom = Color(0xFF0D0B14)
    val Surface = Color(0xFF16131F)
    val SurfaceChip = Color(0xFF1C1826)
    val BorderFaint = Color(0xFF2A2438)

    val Cyan = Color(0xFF00E5FF)
    val Magenta = Color(0xFFFF2D95)
    val Green = Color(0xFF39FF14)
    val Purple = Color(0xFFBF5AF2)

    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFAAAAAA)
    val TextMuted = Color(0xFF888888)
}

/**
 * Raw palette values for light mode. Backgrounds/surfaces/text flip to
 * light tones; accent hues stay the same family as the dark palette (just
 * deepened a touch for contrast on white) so mode buttons, the loudness
 * slider, etc. keep their identity in both themes.
 */
object LightPalette {
    val BgTop = Color(0xFFF7F7FB)
    val BgMid = Color(0xFFEFEFF7)
    val BgBottom = Color(0xFFE6E6F2)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceChip = Color(0xFFEFEDF7)
    val BorderFaint = Color(0xFFDAD7EA)

    val Cyan = Color(0xFF00A7C4)
    val Magenta = Color(0xFFE0158F)
    val Green = Color(0xFF1FA850)
    val Purple = Color(0xFF7C4FE0)

    val TextPrimary = Color(0xFF16152A)
    val TextSecondary = Color(0xFF57566E)
    val TextMuted = Color(0xFF8B8AA0)
}
