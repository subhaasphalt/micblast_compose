package com.example.micblast.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Which light/dark modes a theme supports.
 *  - DARK_ONLY:  theme forces dark mode; the Settings toggle is set to dark
 *                and disabled.
 *  - LIGHT_ONLY: theme forces light mode; the Settings toggle is set to
 *                light and disabled.
 *  - BOTH:       user is free to switch; the Settings toggle is enabled.
 */
enum class ThemeModeSupport {
    DARK_ONLY,
    LIGHT_ONLY,
    BOTH;

    /** True when the user is allowed to flip light/dark while this theme is active. */
    val allowsToggle: Boolean get() = this == BOTH

    /**
     * Resolves the mode that should actually be used for this theme, given
     * what's currently stored/requested. DARK_ONLY/LIGHT_ONLY always win
     * over the requested value; BOTH just passes it through.
     */
    fun resolveDarkTheme(requestedDarkTheme: Boolean): Boolean = when (this) {
        DARK_ONLY -> true
        LIGHT_ONLY -> false
        BOTH -> requestedDarkTheme
    }
}

/**
 * A theme is exactly 6 colors plus a mode-support flag. Nothing about
 * backgrounds, surfaces, or text lives here — those come from
 * [DarkSurfacePalette] / [LightSurfacePalette] and are shared by every
 * theme, so adding a theme never means touching backgrounds.
 *
 *  - accentPrimary / accentSecondary: the swappable pair — hamburger
 *    button, lock icon, title gradient, labels, slider extremes/cursor,
 *    and the quit dialog buttons.
 *  - mode1AccentColor..mode4AccentColor: one color per voice-mode button
 *    (Normal, Chipmunk, Monster, Robot), in that order.
 */
data class ThemeSpec(
    val id: String,
    val displayName: String,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val mode1AccentColor: Color,
    val mode2AccentColor: Color,
    val mode3AccentColor: Color,
    val mode4AccentColor: Color,
    val allowLightDark: ThemeModeSupport,
)

/**
 * The theme registry. This is the single source of truth for what themes
 * exist in the app — add a new [ThemeSpec] entry here and it automatically
 * shows up in Settings (see SettingsScreen's theme picker) with no other
 * wiring required.
 */
val AppThemes: List<ThemeSpec> = listOf(

    // ─────────────────────────────────────────────────────────────
    // 1. Neon Aurora
    // Cyan → Violet
    // Futuristic, vibrant, close to the original theme but smoother.
    // ─────────────────────────────────────────────────────────────
    ThemeSpec(
        id = "neonAurora",
        displayName = "Neon Aurora",
        accentPrimary = Color(0xFF00D9C6),
        accentSecondary = Color(0xFF7C5CFF),
        mode1AccentColor = Color(0xFF00D9C6), // Normal   — aqua
        mode2AccentColor = Color(0xFFFF4FCB), // Chipmunk — pink
        mode3AccentColor = Color(0xFF72E000), // Monster  — lime
        mode4AccentColor = Color(0xFF8B5CF6), // Robot    — violet
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // ─────────────────────────────────────────────────────────────
    // 2. Neon Sunset
    // Orange → Pink
    // Warm nightclub / synthwave feeling.
    // ─────────────────────────────────────────────────────────────
    ThemeSpec(
        id = "neonSunset",
        displayName = "Neon Sunset",
        accentPrimary = Color(0xFFFF6B35),
        accentSecondary = Color(0xFFFF2D95),
        mode1AccentColor = Color(0xFFFFA62B), // Normal   — amber
        mode2AccentColor = Color(0xFFFF3CAC), // Chipmunk — hot pink
        mode3AccentColor = Color(0xFF8CE000), // Monster  — lime
        mode4AccentColor = Color(0xFF9B5CFF), // Robot    — purple
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // ─────────────────────────────────────────────────────────────
    // 3. Miami Vice
    // Cyan → Pink
    // Retro 80s neon, but slightly softer than the original.
    // ─────────────────────────────────────────────────────────────
    ThemeSpec(
        id = "miamiVice",
        displayName = "Miami Vice",
        accentPrimary = Color(0xFF00CFEF),
        accentSecondary = Color(0xFFFF3CAC),
        mode1AccentColor = Color(0xFF00CFEF), // Normal   — pool blue
        mode2AccentColor = Color(0xFFFF3CAC), // Chipmunk — neon pink
        mode3AccentColor = Color(0xFFFFC857), // Monster  — sunset yellow
        mode4AccentColor = Color(0xFF8A5CF6), // Robot    — violet
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // ─────────────────────────────────────────────────────────────
    // 4. Toxic Arcade
    // Lime → Cyan
    // Very playful / gaming / arcade.
    // ─────────────────────────────────────────────────────────────
    ThemeSpec(
        id = "toxicArcade",
        displayName = "Toxic Arcade",
        accentPrimary = Color(0xFF8CE000),
        accentSecondary = Color(0xFF00D9FF),
        mode1AccentColor = Color(0xFF8CE000), // Normal   — toxic lime
        mode2AccentColor = Color(0xFFFF3B81), // Chipmunk — radioactive pink
        mode3AccentColor = Color(0xFF39E600), // Monster  — electric green
        mode4AccentColor = Color(0xFF00B8FF), // Robot    — plasma blue
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // ─────────────────────────────────────────────────────────────
    // 5. Cyberpunk
    // Pink → Cyan
    // Maximum futuristic / nightclub energy.
    // ─────────────────────────────────────────────────────────────
    ThemeSpec(
        id = "cyberpunk",
        displayName = "Cyberpunk",
        accentPrimary = Color(0xFFFF168F),
        accentSecondary = Color(0xFF00D9FF),
        mode1AccentColor = Color(0xFF00D9FF), // Normal   — cyan
        mode2AccentColor = Color(0xFFFF168F), // Chipmunk — neon pink
        mode3AccentColor = Color(0xFFFFD600), // Monster  — electric yellow
        mode4AccentColor = Color(0xFF8F4CFF), // Robot    — ultraviolet
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // ─────────────────────────────────────────────────────────────
    // 6. Electric Candy
    // Pink → Purple
    // Fun, soft, colorful. Especially suitable for a "Funny" app.
    // ─────────────────────────────────────────────────────────────
    ThemeSpec(
        id = "electricCandy",
        displayName = "Electric Candy",
        accentPrimary = Color(0xFFFF3FA4),
        accentSecondary = Color(0xFF7657E8),
        mode1AccentColor = Color(0xFFFF3FA4), // Normal   — bubblegum
        mode2AccentColor = Color(0xFFFF8A3D), // Chipmunk — orange
        mode3AccentColor = Color(0xFF62D900), // Monster  — candy lime
        mode4AccentColor = Color(0xFF9257E8), // Robot    — grape
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // ─────────────────────────────────────────────────────────────
    // 7. Plasma
    // Ice Blue → Hot Pink
    // Sci-fi / holographic.
    // ─────────────────────────────────────────────────────────────
    ThemeSpec(
        id = "plasma",
        displayName = "Plasma",
        accentPrimary = Color(0xFF00CFEF),
        accentSecondary = Color(0xFFFF00B8),
        mode1AccentColor = Color(0xFF00CFEF), // Normal   — plasma blue
        mode2AccentColor = Color(0xFFFF00B8), // Chipmunk — plasma pink
        mode3AccentColor = Color(0xFF72DD00), // Monster  — plasma green
        mode4AccentColor = Color(0xFF7957E8), // Robot    — plasma violet
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // ─────────────────────────────────────────────────────────────
    // 8. Electric Ocean
    // Blue → Teal
    // Cleaner and calmer while retaining personality.
    // ─────────────────────────────────────────────────────────────
    ThemeSpec(
        id = "electricOcean",
        displayName = "Electric Ocean",
        accentPrimary = Color(0xFF008CFF),
        accentSecondary = Color(0xFF00CFA3),
        mode1AccentColor = Color(0xFF008CFF), // Normal   — ocean blue
        mode2AccentColor = Color(0xFF00B8D9), // Chipmunk — cyan
        mode3AccentColor = Color(0xFF62C900), // Monster  — green
        mode4AccentColor = Color(0xFF6857D9), // Robot    — deep violet
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // ─────────────────────────────────────────────────────────────
    // 9. Fire & Ice
    // Blue → Orange
    // Strong complementary contrast.
    // ─────────────────────────────────────────────────────────────
    ThemeSpec(
        id = "fireAndIce",
        displayName = "Fire & Ice",
        accentPrimary = Color(0xFF00AEEF),
        accentSecondary = Color(0xFFFF5A36),
        mode1AccentColor = Color(0xFF00AEEF), // Normal   — ice blue
        mode2AccentColor = Color(0xFFFF4F81), // Chipmunk — pink
        mode3AccentColor = Color(0xFFFFA600), // Monster  — fire orange
        mode4AccentColor = Color(0xFF7657E8), // Robot    — cool violet
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // ─────────────────────────────────────────────────────────────
    // 10. Electric Lemon
    // Yellow → Blue
    // Bright, unusual and extremely recognizable.
    // ─────────────────────────────────────────────────────────────
    ThemeSpec(
        id = "electricLemon",
        displayName = "Electric Lemon",
        accentPrimary = Color(0xFFFFC800),
        accentSecondary = Color(0xFF2878FF),
        mode1AccentColor = Color(0xFFFFC800), // Normal   — lemon
        mode2AccentColor = Color(0xFFFF4F9A), // Chipmunk — pink
        mode3AccentColor = Color(0xFF62C900), // Monster  — lime
        mode4AccentColor = Color(0xFF2878FF), // Robot    — electric blue
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // ─────────────────────────────────────────────────────────────
    // 11. Mint Pop
    // Mint → Coral
    // One of the more LIGHT-MODE-FRIENDLY themes.
    // Fresh and less "gaming RGB".
    // ─────────────────────────────────────────────────────────────
    ThemeSpec(
        id = "mintPop",
        displayName = "Mint Pop",
        accentPrimary = Color(0xFF00A889),
        accentSecondary = Color(0xFFFF5272),
        mode1AccentColor = Color(0xFF00A889), // Normal   — mint
        mode2AccentColor = Color(0xFFFF5272), // Chipmunk — coral pink
        mode3AccentColor = Color(0xFF5EAD00), // Monster  — green
        mode4AccentColor = Color(0xFF7654C7), // Robot    — purple
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // ─────────────────────────────────────────────────────────────
    // 12. Berry
    // Raspberry → Indigo
    // Strong on dark, excellent contrast on light.
    // ─────────────────────────────────────────────────────────────
    ThemeSpec(
        id = "berry",
        displayName = "Berry",
        accentPrimary = Color(0xFFE91E63),
        accentSecondary = Color(0xFF5E5CE6),
        mode1AccentColor = Color(0xFFE91E63), // Normal   — raspberry
        mode2AccentColor = Color(0xFFFF7043), // Chipmunk — coral
        mode3AccentColor = Color(0xFF43A047), // Monster  — green
        mode4AccentColor = Color(0xFF6C4CC5), // Robot    — indigo
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // ─────────────────────────────────────────────────────────────
    // 13. Tropical
    // Teal → Orange
    // Cheerful, colorful, less cyberpunk.
    // ─────────────────────────────────────────────────────────────
    ThemeSpec(
        id = "tropical",
        displayName = "Tropical",
        accentPrimary = Color(0xFF00A896),
        accentSecondary = Color(0xFFFF7043),
        mode1AccentColor = Color(0xFF00A896), // Normal   — tropical teal
        mode2AccentColor = Color(0xFFFF4F81), // Chipmunk — hibiscus pink
        mode3AccentColor = Color(0xFF65B000), // Monster  — palm green
        mode4AccentColor = Color(0xFF6355C7), // Robot    — tropical purple
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // ─────────────────────────────────────────────────────────────
    // 14. Grape Soda
    // Purple → Pink
    // Playful and slightly retro.
    // ─────────────────────────────────────────────────────────────
    ThemeSpec(
        id = "grapeSoda",
        displayName = "Grape Soda",
        accentPrimary = Color(0xFF7B4DFF),
        accentSecondary = Color(0xFFFF3D9A),
        mode1AccentColor = Color(0xFF7B4DFF), // Normal   — grape
        mode2AccentColor = Color(0xFFFF3D9A), // Chipmunk — strawberry
        mode3AccentColor = Color(0xFF64B500), // Monster  — lime
        mode4AccentColor = Color(0xFF008FC4), // Robot    — blue
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // ─────────────────────────────────────────────────────────────
    // 15. Peach Fizz
    // Coral → Blue
    // Very light-mode-friendly and more "consumer app" than gamer.
    // ─────────────────────────────────────────────────────────────
    ThemeSpec(
        id = "peachFizz",
        displayName = "Peach Fizz",
        accentPrimary = Color(0xFFFF5F56),
        accentSecondary = Color(0xFF3478F6),
        mode1AccentColor = Color(0xFFFF5F56), // Normal   — peach/coral
        mode2AccentColor = Color(0xFFE83E8C), // Chipmunk — berry
        mode3AccentColor = Color(0xFF4DAD00), // Monster  — green
        mode4AccentColor = Color(0xFF6750A4), // Robot    — purple
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // ─────────────────────────────────────────────────────────────
    // 16. Monochrome Pop
    // Blue → Pink
    // Restrained global palette, colorful modes.
    // ─────────────────────────────────────────────────────────────
    ThemeSpec(
        id = "monochromePop",
        displayName = "Monochrome Pop",
        accentPrimary = Color(0xFF3D8BFF),
        accentSecondary = Color(0xFFE83E8C),
        mode1AccentColor = Color(0xFF3D8BFF), // Normal   — blue
        mode2AccentColor = Color(0xFFE83E8C), // Chipmunk — pink
        mode3AccentColor = Color(0xFF4CAF50), // Monster  — green
        mode4AccentColor = Color(0xFF7E57C2), // Robot    — purple
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // ─────────────────────────────────────────────────────────────
    // 17. Retro Arcade
    // Red → Blue
    // Classic arcade-machine palette.
    // ─────────────────────────────────────────────────────────────
    ThemeSpec(
        id = "retroArcade",
        displayName = "Retro Arcade",
        accentPrimary = Color(0xFFFF1744),
        accentSecondary = Color(0xFF2979FF),
        mode1AccentColor = Color(0xFFFF1744), // Normal   — arcade red
        mode2AccentColor = Color(0xFFFFEA00), // Chipmunk — yellow
        mode3AccentColor = Color(0xFF00C853), // Monster  — green
        mode4AccentColor = Color(0xFFAA00FF), // Robot    — purple
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // ─────────────────────────────────────────────────────────────
    // 18. Holographic
    // Cyan → Pink
    // The "premium" evolution of the original theme.
    // ─────────────────────────────────────────────────────────────
    ThemeSpec(
        id = "holographic",
        displayName = "Holographic",
        accentPrimary = Color(0xFF00BFD8),
        accentSecondary = Color(0xFFE83EBC),
        mode1AccentColor = Color(0xFF00BFD8), // Normal   — cyan
        mode2AccentColor = Color(0xFFE83EBC), // Chipmunk — holographic pink
        mode3AccentColor = Color(0xFF65C900), // Monster  — holographic green
        mode4AccentColor = Color(0xFF7657D9), // Robot    — holographic violet
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // ─────────────────────────────────────────────────────────────
    // 19. Soft Neon
    // Teal → Rose
    // Specifically intended to test a softer visual identity.
    // ─────────────────────────────────────────────────────────────
    ThemeSpec(
        id = "softNeon",
        displayName = "Soft Neon",
        accentPrimary = Color(0xFF00AFA0),
        accentSecondary = Color(0xFFE84A8A),
        mode1AccentColor = Color(0xFF00AFA0), // Normal   — teal
        mode2AccentColor = Color(0xFFE84A8A), // Chipmunk — rose
        mode3AccentColor = Color(0xFF65AD00), // Monster  — green
        mode4AccentColor = Color(0xFF7055C7), // Robot    — violet
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // ─────────────────────────────────────────────────────────────
    // 20. Rainbow Reactor
    // Cyan → Magenta
    // Maximum "party mode".
    // ─────────────────────────────────────────────────────────────
    ThemeSpec(
        id = "rainbowReactor",
        displayName = "Rainbow Reactor",
        accentPrimary = Color(0xFF00D5E8),
        accentSecondary = Color(0xFFFF1493),
        mode1AccentColor = Color(0xFF00D5E8), // Normal   — cyan
        mode2AccentColor = Color(0xFFFF1493), // Chipmunk — pink
        mode3AccentColor = Color(0xFF72D900), // Monster  — green
        mode4AccentColor = Color(0xFFFFA000), // Robot    — orange
        allowLightDark = ThemeModeSupport.BOTH,
    ),
)

val DefaultTheme: ThemeSpec = AppThemes.first()

fun themeById(id: String): ThemeSpec =
    AppThemes.firstOrNull { it.id == id } ?: DefaultTheme
