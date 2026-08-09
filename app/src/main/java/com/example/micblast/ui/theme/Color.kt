package com.example.micblast.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Background / surface / text values for dark mode. These are shared by
 * EVERY theme — mode (light/dark) controls luminance only, themes control
 * accents only. See [Themes.kt] for the accent side of things.
 */
object DarkSurfacePalette {
    val BgTop = Color(0xFF0B0F1A)
    val BgMid = Color(0xFF12101F)
    val BgBottom = Color(0xFF0D0B14)
    val Surface = Color(0xFF16131F)
    val SurfaceChip = Color(0xFF1C1826)
    val BorderFaint = Color(0xFF2A2438)

    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFAAAAAA)
    val TextMuted = Color(0xFF888888)
}

/**
 * Background / surface / text values for light mode. Shared by every theme,
 * same as [DarkSurfacePalette].
 */
object LightSurfacePalette {
    val BgTop = Color(0xFFF7F7FB)
    val BgMid = Color(0xFFEFEFF7)
    val BgBottom = Color(0xFFE6E6F2)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceChip = Color(0xFFEFEDF7)
    val BorderFaint = Color(0xFFDAD7EA)

    val TextPrimary = Color(0xFF16152A)
    val TextSecondary = Color(0xFF57566E)
    val TextMuted = Color(0xFF8B8AA0)
}
