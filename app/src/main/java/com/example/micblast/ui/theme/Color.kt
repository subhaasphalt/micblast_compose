package com.example.micblast.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Theme identifiers. Each maps to a complete [MicBlastColors] palette.
 * Some are dark, one is light (Soft Pastel).
 */
enum class AppTheme(val id: String, val displayName: String, val isDark: Boolean) {
    NEON_PARTY("neon_party", "Neon Party", true),
    SYNTHWAVE("synthwave", "Synthwave", true),
    CARTOON("cartoon", "Cartoon Chaos", true),
    CONCERT("concert", "Concert Stage", true),
    ROBOT_LAB("robot_lab", "Robot Lab", true),
    PASTEL("pastel", "Soft Pastel", false);

    companion object {
        fun fromId(id: String?): AppTheme =
            entries.find { it.id == id } ?: NEON_PARTY
    }
}

// ---------------------------------------------------------------------------
// 1. Neon Party  (refined original)
// ---------------------------------------------------------------------------
object NeonPartyPalette {
    val BgTop = Color(0xFF0A0E1A)
    val BgMid = Color(0xFF12101F)
    val BgBottom = Color(0xFF0D0B16)
    val Surface = Color(0xFF16131F)
    val SurfaceChip = Color(0xFF1C1828)
    val BorderFaint = Color(0xFF2A243A)

    val Cyan = Color(0xFF00F0FF)
    val Magenta = Color(0xFFFF2D95)
    val Green = Color(0xFF39FF14)
    val Purple = Color(0xFFC84BFF)

    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB0B0C0)
    val TextMuted = Color(0xFF8888A0)
}

// ---------------------------------------------------------------------------
// 2. Synthwave / 80s Retro
// ---------------------------------------------------------------------------
object SynthwavePalette {
    val BgTop = Color(0xFF0D0221)
    val BgMid = Color(0xFF1A0A2E)
    val BgBottom = Color(0xFF12051F)
    val Surface = Color(0xFF1E1035)
    val SurfaceChip = Color(0xFF2A1848)
    val BorderFaint = Color(0xFF3D2566)

    val Cyan = Color(0xFF00F5FF)
    val Magenta = Color(0xFFFF00A0)
    val Green = Color(0xFF39FF14)
    val Purple = Color(0xFFBF5AF2)

    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFD0A0FF)
    val TextMuted = Color(0xFF9A70C0)
}

// ---------------------------------------------------------------------------
// 3. Cartoon Chaos (dark)
// ---------------------------------------------------------------------------
object CartoonPalette {
    val BgTop = Color(0xFF1A0F2E)
    val BgMid = Color(0xFF251A3A)
    val BgBottom = Color(0xFF1E1433)
    val Surface = Color(0xFF2E2248)
    val SurfaceChip = Color(0xFF3A2C5A)
    val BorderFaint = Color(0xFF4A3A6E)

    val Cyan = Color(0xFF00E8FF)
    val Magenta = Color(0xFFFF4DC4)
    val Green = Color(0xFF7CFF3A)
    val Purple = Color(0xFFB46BFF)

    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFE0C8FF)
    val TextMuted = Color(0xFFB090D0)
}

// ---------------------------------------------------------------------------
// 4. Concert Stage
// ---------------------------------------------------------------------------
object ConcertPalette {
    val BgTop = Color(0xFF0A0A0F)
    val BgMid = Color(0xFF121218)
    val BgBottom = Color(0xFF0E0E14)
    val Surface = Color(0xFF1A1A22)
    val SurfaceChip = Color(0xFF22222C)
    val BorderFaint = Color(0xFF333340)

    val Cyan = Color(0xFF4FC3F7)
    val Magenta = Color(0xFFFF4081)
    val Green = Color(0xFF76FF03)
    val Purple = Color(0xFFE040FB)

    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB0B0C0)
    val TextMuted = Color(0xFF808090)
}

// ---------------------------------------------------------------------------
// 5. Robot Lab
// ---------------------------------------------------------------------------
object RobotLabPalette {
    val BgTop = Color(0xFF0A1218)
    val BgMid = Color(0xFF0F1A22)
    val BgBottom = Color(0xFF0C161C)
    val Surface = Color(0xFF152028)
    val SurfaceChip = Color(0xFF1C2A34)
    val BorderFaint = Color(0xFF2A3A48)

    val Cyan = Color(0xFF00E5FF)
    val Magenta = Color(0xFF00B8D4)
    val Green = Color(0xFF00E676)
    val Purple = Color(0xFF7C4DFF)

    val TextPrimary = Color(0xFFE8F4F8)
    val TextSecondary = Color(0xFFA0C0D0)
    val TextMuted = Color(0xFF708090)
}

// ---------------------------------------------------------------------------
// 6. Soft Pastel Party (light)
// ---------------------------------------------------------------------------
object PastelPalette {
    val BgTop = Color(0xFFF8F4FF)
    val BgMid = Color(0xFFF0ECFF)
    val BgBottom = Color(0xFFE8E4FA)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceChip = Color(0xFFF5F0FF)
    val BorderFaint = Color(0xFFD8D0F0)

    val Cyan = Color(0xFF00B4D8)
    val Magenta = Color(0xFFE91E8C)
    val Green = Color(0xFF43A047)
    val Purple = Color(0xFF7B1FA2)

    val TextPrimary = Color(0xFF1A1428)
    val TextSecondary = Color(0xFF4A4060)
    val TextMuted = Color(0xFF7A7090)
}

/** Convenience factory that turns any [AppTheme] into a full [MicBlastColors]. */
fun AppTheme.toColors(): MicBlastColors = when (this) {
    AppTheme.NEON_PARTY -> MicBlastColors(
        bgTop = NeonPartyPalette.BgTop,
        bgMid = NeonPartyPalette.BgMid,
        bgBottom = NeonPartyPalette.BgBottom,
        surface = NeonPartyPalette.Surface,
        surfaceChip = NeonPartyPalette.SurfaceChip,
        borderFaint = NeonPartyPalette.BorderFaint,
        accentCyan = NeonPartyPalette.Cyan,
        accentMagenta = NeonPartyPalette.Magenta,
        accentGreen = NeonPartyPalette.Green,
        accentPurple = NeonPartyPalette.Purple,
        textPrimary = NeonPartyPalette.TextPrimary,
        textSecondary = NeonPartyPalette.TextSecondary,
        textMuted = NeonPartyPalette.TextMuted,
    )
    AppTheme.SYNTHWAVE -> MicBlastColors(
        bgTop = SynthwavePalette.BgTop,
        bgMid = SynthwavePalette.BgMid,
        bgBottom = SynthwavePalette.BgBottom,
        surface = SynthwavePalette.Surface,
        surfaceChip = SynthwavePalette.SurfaceChip,
        borderFaint = SynthwavePalette.BorderFaint,
        accentCyan = SynthwavePalette.Cyan,
        accentMagenta = SynthwavePalette.Magenta,
        accentGreen = SynthwavePalette.Green,
        accentPurple = SynthwavePalette.Purple,
        textPrimary = SynthwavePalette.TextPrimary,
        textSecondary = SynthwavePalette.TextSecondary,
        textMuted = SynthwavePalette.TextMuted,
    )
    AppTheme.CARTOON -> MicBlastColors(
        bgTop = CartoonPalette.BgTop,
        bgMid = CartoonPalette.BgMid,
        bgBottom = CartoonPalette.BgBottom,
        surface = CartoonPalette.Surface,
        surfaceChip = CartoonPalette.SurfaceChip,
        borderFaint = CartoonPalette.BorderFaint,
        accentCyan = CartoonPalette.Cyan,
        accentMagenta = CartoonPalette.Magenta,
        accentGreen = CartoonPalette.Green,
        accentPurple = CartoonPalette.Purple,
        textPrimary = CartoonPalette.TextPrimary,
        textSecondary = CartoonPalette.TextSecondary,
        textMuted = CartoonPalette.TextMuted,
    )
    AppTheme.CONCERT -> MicBlastColors(
        bgTop = ConcertPalette.BgTop,
        bgMid = ConcertPalette.BgMid,
        bgBottom = ConcertPalette.BgBottom,
        surface = ConcertPalette.Surface,
        surfaceChip = ConcertPalette.SurfaceChip,
        borderFaint = ConcertPalette.BorderFaint,
        accentCyan = ConcertPalette.Cyan,
        accentMagenta = ConcertPalette.Magenta,
        accentGreen = ConcertPalette.Green,
        accentPurple = ConcertPalette.Purple,
        textPrimary = ConcertPalette.TextPrimary,
        textSecondary = ConcertPalette.TextSecondary,
        textMuted = ConcertPalette.TextMuted,
    )
    AppTheme.ROBOT_LAB -> MicBlastColors(
        bgTop = RobotLabPalette.BgTop,
        bgMid = RobotLabPalette.BgMid,
        bgBottom = RobotLabPalette.BgBottom,
        surface = RobotLabPalette.Surface,
        surfaceChip = RobotLabPalette.SurfaceChip,
        borderFaint = RobotLabPalette.BorderFaint,
        accentCyan = RobotLabPalette.Cyan,
        accentMagenta = RobotLabPalette.Magenta,
        accentGreen = RobotLabPalette.Green,
        accentPurple = RobotLabPalette.Purple,
        textPrimary = RobotLabPalette.TextPrimary,
        textSecondary = RobotLabPalette.TextSecondary,
        textMuted = RobotLabPalette.TextMuted,
    )
    AppTheme.PASTEL -> MicBlastColors(
        bgTop = PastelPalette.BgTop,
        bgMid = PastelPalette.BgMid,
        bgBottom = PastelPalette.BgBottom,
        surface = PastelPalette.Surface,
        surfaceChip = PastelPalette.SurfaceChip,
        borderFaint = PastelPalette.BorderFaint,
        accentCyan = PastelPalette.Cyan,
        accentMagenta = PastelPalette.Magenta,
        accentGreen = PastelPalette.Green,
        accentPurple = PastelPalette.Purple,
        textPrimary = PastelPalette.TextPrimary,
        textSecondary = PastelPalette.TextSecondary,
        textMuted = PastelPalette.TextMuted,
    )
}
