package com.example.micblast.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Accent pair presets. Each theme only changes the two primary gradient
 * colors (start = accentCyan role, end = accentMagenta role). Backgrounds
 * and text stay driven by dark/light mode so pairs feel distinct without
 * the whole UI looking different.
 */
enum class AccentTheme(
    val id: String,
    val displayName: String,
    val start: Color,   // plays the role of "cyan" in gradients
    val end: Color,     // plays the role of "magenta" in gradients
) {
    CLASSIC(
        id = "classic",
        displayName = "Classic",
        start = Color(0xFF00F0FF),
        end = Color(0xFFFF2D95),
    ),
    SYNTH(
        id = "synth",
        displayName = "Synth",
        start = Color(0xFF00B4FF),
        end = Color(0xFFFF2E9A),
    ),
    TOXIC(
        id = "toxic",
        displayName = "Toxic",
        start = Color(0xFF39FF14),
        end = Color(0xFFBF5AF2),
    ),
    SUNSET(
        id = "sunset",
        displayName = "Sunset",
        start = Color(0xFFFF8A00),
        end = Color(0xFFFF3D6E),
    ),
    OCEAN(
        id = "ocean",
        displayName = "Ocean",
        start = Color(0xFF00E0C6),
        end = Color(0xFF4B7BFF),
    ),
    VIOLET(
        id = "violet",
        displayName = "Violet",
        start = Color(0xFF5AC8FF),
        end = Color(0xFFA855F7),
    );

    companion object {
        fun fromId(id: String?): AccentTheme =
            entries.find { it.id == id } ?: CLASSIC
    }
}

// ---------------------------------------------------------------------------
// Dark / Light base palettes (backgrounds, surfaces, text)
// ---------------------------------------------------------------------------

object DarkBase {
    val BgTop = Color(0xFF0A0E1A)
    val BgMid = Color(0xFF12101F)
    val BgBottom = Color(0xFF0D0B16)
    val Surface = Color(0xFF16131F)
    val SurfaceChip = Color(0xFF1C1828)
    val BorderFaint = Color(0xFF2A243A)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB0B0C0)
    val TextMuted = Color(0xFF8888A0)
}

object LightBase {
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

/**
 * Builds a full [MicBlastColors] from dark/light + the chosen accent pair.
 * Green / Purple stay as supporting accents so mode buttons still have
 * variety without fighting the main gradient.
 */
fun buildColors(darkTheme: Boolean, accent: AccentTheme): MicBlastColors {
    val supportGreen = if (darkTheme) Color(0xFF39FF14) else Color(0xFF1FA850)
    val supportPurple = if (darkTheme) Color(0xFFBF5AF2) else Color(0xFF7C4FE0)

    return if (darkTheme) {
        MicBlastColors(
            bgTop = DarkBase.BgTop,
            bgMid = DarkBase.BgMid,
            bgBottom = DarkBase.BgBottom,
            surface = DarkBase.Surface,
            surfaceChip = DarkBase.SurfaceChip,
            borderFaint = DarkBase.BorderFaint,
            accentCyan = accent.start,
            accentMagenta = accent.end,
            accentGreen = supportGreen,
            accentPurple = supportPurple,
            textPrimary = DarkBase.TextPrimary,
            textSecondary = DarkBase.TextSecondary,
            textMuted = DarkBase.TextMuted,
        )
    } else {
        MicBlastColors(
            bgTop = LightBase.BgTop,
            bgMid = LightBase.BgMid,
            bgBottom = LightBase.BgBottom,
            surface = LightBase.Surface,
            surfaceChip = LightBase.SurfaceChip,
            borderFaint = LightBase.BorderFaint,
            accentCyan = accent.start,
            accentMagenta = accent.end,
            accentGreen = supportGreen,
            accentPurple = supportPurple,
            textPrimary = LightBase.TextPrimary,
            textSecondary = LightBase.TextSecondary,
            textMuted = LightBase.TextMuted,
        )
    }
}
