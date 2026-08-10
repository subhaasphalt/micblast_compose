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
    ThemeSpec(
        id = "theme1",
        displayName = "Neon Fusion",
        accentPrimary = Color(0xFF00E5FF),
        accentSecondary = Color(0xFFFF2D95),
        mode1AccentColor = Color(0xFF00E5FF),
        mode2AccentColor = Color(0xFFFF2D95),
        mode3AccentColor = Color(0xFF39FF14),
        mode4AccentColor = Color(0xFFBF5AF2),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // 2. Inferno — Red
    ThemeSpec(
        id = "theme2",
        displayName = "Inferno",
        accentPrimary = Color(0xFFFF1744),
        accentSecondary = Color(0xFFFF9100),
        mode1AccentColor = Color(0xFFFF1744),
        mode2AccentColor = Color(0xFFFF9100),
        mode3AccentColor = Color(0xFFFFEA00),
        mode4AccentColor = Color(0xFFFF4081),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // 3. Cyberpunk — Orange
    ThemeSpec(
        id = "theme3",
        displayName = "Cyberpunk",
        accentPrimary = Color(0xFFFF6D00),
        accentSecondary = Color(0xFFD500F9),
        mode1AccentColor = Color(0xFFFF6D00),
        mode2AccentColor = Color(0xFFD500F9),
        mode3AccentColor = Color(0xFF00E5FF),
        mode4AccentColor = Color(0xFFFF2D95),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // 4. Toxic — Yellow/Green
    ThemeSpec(
        id = "theme4",
        displayName = "Toxic",
        accentPrimary = Color(0xFFB6FF00),
        accentSecondary = Color(0xFF00FF66),
        mode1AccentColor = Color(0xFFB6FF00),
        mode2AccentColor = Color(0xFF00FF66),
        mode3AccentColor = Color(0xFFFFFF00),
        mode4AccentColor = Color(0xFF00E5FF),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // 5. Oceanic — Green/Blue
    ThemeSpec(
        id = "theme5",
        displayName = "Oceanic",
        accentPrimary = Color(0xFF00D4A8),
        accentSecondary = Color(0xFF00A8FF),
        mode1AccentColor = Color(0xFF00D4A8),
        mode2AccentColor = Color(0xFF00A8FF),
        mode3AccentColor = Color(0xFF76FF03),
        mode4AccentColor = Color(0xFF536DFE),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // 6. Aurora — Blue/Indigo
    ThemeSpec(
        id = "theme6",
        displayName = "Aurora",
        accentPrimary = Color(0xFF00B8D4),
        accentSecondary = Color(0xFF7C4DFF),
        mode1AccentColor = Color(0xFF00B8D4),
        mode2AccentColor = Color(0xFF7C4DFF),
        mode3AccentColor = Color(0xFF00E676),
        mode4AccentColor = Color(0xFFE040FB),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // 7. Synthwave — Indigo/Violet
    ThemeSpec(
        id = "theme7",
        displayName = "Synthwave",
        accentPrimary = Color(0xFFFF00A8),
        accentSecondary = Color(0xFF7B2FFF),
        mode1AccentColor = Color(0xFFFF00A8),
        mode2AccentColor = Color(0xFF7B2FFF),
        mode3AccentColor = Color(0xFF00F0FF),
        mode4AccentColor = Color(0xFFFF6EC7),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // 8. Royal — Violet
    ThemeSpec(
        id = "theme8",
        displayName = "Royal",
        accentPrimary = Color(0xFF9C27FF),
        accentSecondary = Color(0xFFFFC107),
        mode1AccentColor = Color(0xFF9C27FF),
        mode2AccentColor = Color(0xFFFFC107),
        mode3AccentColor = Color(0xFF00E5FF),
        mode4AccentColor = Color(0xFFFF4081),
        allowLightDark = ThemeModeSupport.BOTH,
    ),
)

val DefaultTheme: ThemeSpec = AppThemes.first()

fun themeById(id: String): ThemeSpec =
    AppThemes.firstOrNull { it.id == id } ?: DefaultTheme
