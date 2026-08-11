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

    // 1. Neon — high contrast / colorful gradient
    ThemeSpec(
        id = "neon",
        displayName = "Neon",
        accentPrimary = Color(0xFF00E5FF),      // Cyan
        accentSecondary = Color(0xFFFF2D95),    // Magenta
        mode1AccentColor = Color(0xFF00E5FF),
        mode2AccentColor = Color(0xFFFF2D95),
        mode3AccentColor = Color(0xFF39FF14),
        mode4AccentColor = Color(0xFFBF5AF2),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // 2. Ocean — analogous
    ThemeSpec(
        id = "ocean",
        displayName = "Ocean",
        accentPrimary = Color(0xFF0077B6),      // Deep blue
        accentSecondary = Color(0xFF00B4D8),    // Teal
        mode1AccentColor = Color(0xFF0077B6),
        mode2AccentColor = Color(0xFF00B4D8),
        mode3AccentColor = Color(0xFF48CAE4),
        mode4AccentColor = Color(0xFF4361EE),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // 3. Forest — analogous / natural
    ThemeSpec(
        id = "forest",
        displayName = "Forest",
        accentPrimary = Color(0xFF008F5A),      // Emerald
        accentSecondary = Color(0xFF8BC34A),    // Lime
        mode1AccentColor = Color(0xFF008F5A),
        mode2AccentColor = Color(0xFF8BC34A),
        mode3AccentColor = Color(0xFF20C997),
        mode4AccentColor = Color(0xFFFFB703),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // 4. Sunset — warm analogous
    ThemeSpec(
        id = "sunset",
        displayName = "Sunset",
        accentPrimary = Color(0xFFFF6B35),      // Orange
        accentSecondary = Color(0xFFFF3B5C),    // Coral/red
        mode1AccentColor = Color(0xFFFF6B35),
        mode2AccentColor = Color(0xFFFF3B5C),
        mode3AccentColor = Color(0xFFFFC107),
        mode4AccentColor = Color(0xFFFF7EB6),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // 5. Royal — complementary / luxurious
    ThemeSpec(
        id = "royal",
        displayName = "Royal",
        accentPrimary = Color(0xFF8A2BE2),      // Violet
        accentSecondary = Color(0xFFFFC107),    // Gold
        mode1AccentColor = Color(0xFF8A2BE2),
        mode2AccentColor = Color(0xFFFFC107),
        mode3AccentColor = Color(0xFF4169E1),
        mode4AccentColor = Color(0xFFFF4F81),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // 6. Berry — shades / rich colors
    ThemeSpec(
        id = "berry",
        displayName = "Berry",
        accentPrimary = Color(0xFFD81B60),      // Raspberry
        accentSecondary = Color(0xFF6A1B9A),    // Plum
        mode1AccentColor = Color(0xFFD81B60),
        mode2AccentColor = Color(0xFF6A1B9A),
        mode3AccentColor = Color(0xFFE91E63),
        mode4AccentColor = Color(0xFF9C27B0),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // 7. Monochrome — monochromatic / black & shades
    ThemeSpec(
        id = "monochrome",
        displayName = "Monochrome",
        accentPrimary = Color(0xFF212121),      // Black
        accentSecondary = Color(0xFF9E9E9E),    // Silver
        mode1AccentColor = Color(0xFF424242),
        mode2AccentColor = Color(0xFF757575),
        mode3AccentColor = Color(0xFFBDBDBD),
        mode4AccentColor = Color(0xFFE0E0E0),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // 8. Pastel — muted / soft
    ThemeSpec(
        id = "pastel",
        displayName = "Pastel",
        accentPrimary = Color(0xFFB39DDB),      // Lavender
        accentSecondary = Color(0xFFFFAB91),    // Peach
        mode1AccentColor = Color(0xFF80CBC4),   // Mint
        mode2AccentColor = Color(0xFF90CAF9),   // Soft blue
        mode3AccentColor = Color(0xFFF48FB1),   // Pink
        mode4AccentColor = Color(0xFFFFE082),   // Yellow
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // 9. Cyberpunk — split complementary / high contrast
    ThemeSpec(
        id = "cyberpunk",
        displayName = "Cyberpunk",
        accentPrimary = Color(0xFFB000FF),      // Electric purple
        accentSecondary = Color(0xFFFF6D00),    // Orange
        mode1AccentColor = Color(0xFFB000FF),
        mode2AccentColor = Color(0xFFFF6D00),
        mode3AccentColor = Color(0xFF00E5FF),
        mode4AccentColor = Color(0xFFFF2D95),
        allowLightDark = ThemeModeSupport.DARK_ONLY,
    ),

    // 10. Aurora — colorful gradient / atmospheric
    ThemeSpec(
        id = "aurora",
        displayName = "Aurora",
        accentPrimary = Color(0xFF00B8D4),      // Teal
        accentSecondary = Color(0xFF7C4DFF),    // Violet
        mode1AccentColor = Color(0xFF00BCD4),
        mode2AccentColor = Color(0xFF7C4DFF),
        mode3AccentColor = Color(0xFF00E676),
        mode4AccentColor = Color(0xFFE040FB),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // 11. Citrus — bright / playful
    ThemeSpec(
        id = "citrus",
        displayName = "Citrus",
        accentPrimary = Color(0xFFFFC107),      // Lemon
        accentSecondary = Color(0xFFFF6D00),    // Orange
        mode1AccentColor = Color(0xFFFFC107),
        mode2AccentColor = Color(0xFFFF6D00),
        mode3AccentColor = Color(0xFF8BC34A),
        mode4AccentColor = Color(0xFFFF5252),
        allowLightDark = ThemeModeSupport.LIGHT_ONLY,
    ),

    // 12. High Contrast — complementary
    ThemeSpec(
        id = "high_contrast",
        displayName = "High Contrast",
        accentPrimary = Color(0xFFFF1744),      // Red
        accentSecondary = Color(0xFF00B8D4),    // Cyan
        mode1AccentColor = Color(0xFFFF1744),
        mode2AccentColor = Color(0xFF00B8D4),
        mode3AccentColor = Color(0xFFFFD600),
        mode4AccentColor = Color(0xFF00C853),
        allowLightDark = ThemeModeSupport.BOTH,
    ),
)

val DefaultTheme: ThemeSpec = AppThemes.first()

fun themeById(id: String): ThemeSpec =
    AppThemes.firstOrNull { it.id == id } ?: DefaultTheme
