package com.example.micblast.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Background / surface / text values for one light/dark mode. Shared by
 * EVERY theme — mode controls luminance only, themes control accents only
 * (see [ThemeSpec] in Themes.kt).
 */
data class ModeSurfacePalette(
    val BgTop: Color,
    val BgMid: Color,
    val BgBottom: Color,
    val Surface: Color,
    val SurfaceChip: Color,
    val BorderFaint: Color,
    val TextPrimary: Color,
    val TextSecondary: Color,
    val TextMuted: Color,
)

val DarkSurfacePalette = ModeSurfacePalette(
    BgTop = Color(0xFF0B0F1A),
    BgMid = Color(0xFF12101F),
    BgBottom = Color(0xFF0D0B14),
    Surface = Color(0xFF16131F),
    SurfaceChip = Color(0xFF1C1826),
    BorderFaint = Color(0xFF2A2438),
    TextPrimary = Color(0xFFFFFFFF),
    TextSecondary = Color(0xFFAAAAAA),
    TextMuted = Color(0xFF888888),
)

val LightSurfacePalette = ModeSurfacePalette(
    BgTop = Color(0xFFF7F7FB),
    BgMid = Color(0xFFEFEFF7),
    BgBottom = Color(0xFFE6E6F2),
    Surface = Color(0xFFFFFFFF),
    SurfaceChip = Color(0xFFEFEDF7),
    BorderFaint = Color(0xFFDAD7EA),
    TextPrimary = Color(0xFF16152A),
    TextSecondary = Color(0xFF57566E),
    TextMuted = Color(0xFF8B8AA0),
)
