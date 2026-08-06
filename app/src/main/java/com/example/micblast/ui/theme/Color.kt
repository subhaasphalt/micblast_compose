package com.example.micblast.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Raw palette values. These mirror the old res/values/colors.xml so nothing
 * shifts visually during the Compose migration. Once theming lands, this
 * becomes one of several palettes (e.g. NeonPalette, LightPalette,
 * CustomPalette) that MicBlastColors can be built from.
 */
object NeonPalette {
    val BgTop = Color(0xFF0B0A1A)
    val BgBottom = Color(0xFF000005)
    val Surface = Color(0xFF13122A)
    val SurfaceAlt = Color(0xFF1B1A38)
    val BorderFaint = Color(0xFF332F5C)

    val Cyan = Color(0xFF00E8FF)
    val Magenta = Color(0xFFFF2FD1)
    val Green = Color(0xFF39FF9E)
    val Purple = Color(0xFF7C5CFF)

    val TextPrimary = Color(0xFFF3F4FF)
    val TextSecondary = Color(0xFF9AA0C9)
}
