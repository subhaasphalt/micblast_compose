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
        displayName = "Theme 1",
        accentPrimary = Color(0xFF00E5FF),
        accentSecondary = Color(0xFFFF2D95),
        mode1AccentColor = Color(0xFF00E5FF),
        mode2AccentColor = Color(0xFFFF2D95),
        mode3AccentColor = Color(0xFF39FF14),
        mode4AccentColor = Color(0xFFBF5AF2),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    ThemeSpec(
        id = "theme2",
        displayName = "Red",
        accentPrimary = Color(0xFFFF1744),
        accentSecondary = Color(0xFFFF5252),
        mode1AccentColor = Color(0xFFFF1744),
        mode2AccentColor = Color(0xFFFF5252),
        mode3AccentColor = Color(0xFF39FF14),
        mode4AccentColor = Color(0xFFBF5AF2),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    ThemeSpec(
        id = "theme3",
        displayName = "Orange",
        accentPrimary = Color(0xFFFF6D00),
        accentSecondary = Color(0xFFFFAB00),
        mode1AccentColor = Color(0xFFFF6D00),
        mode2AccentColor = Color(0xFFFFAB00),
        mode3AccentColor = Color(0xFF39FF14),
        mode4AccentColor = Color(0xFFBF5AF2),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    ThemeSpec(
        id = "theme4",
        displayName = "Yellow",
        accentPrimary = Color(0xFFFFD600),
        accentSecondary = Color(0xFFFFFF00),
        mode1AccentColor = Color(0xFFFFD600),
        mode2AccentColor = Color(0xFFFFFF00),
        mode3AccentColor = Color(0xFF39FF14),
        mode4AccentColor = Color(0xFFBF5AF2),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    ThemeSpec(
        id = "theme5",
        displayName = "Green",
        accentPrimary = Color(0xFF00C853),
        accentSecondary = Color(0xFF69F0AE),
        mode1AccentColor = Color(0xFF00C853),
        mode2AccentColor = Color(0xFF69F0AE),
        mode3AccentColor = Color(0xFF39FF14),
        mode4AccentColor = Color(0xFFBF5AF2),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    ThemeSpec(
        id = "theme6",
        displayName = "Blue",
        accentPrimary = Color(0xFF2979FF),
        accentSecondary = Color(0xFF448AFF),
        mode1AccentColor = Color(0xFF2979FF),
        mode2AccentColor = Color(0xFF448AFF),
        mode3AccentColor = Color(0xFF39FF14),
        mode4AccentColor = Color(0xFFBF5AF2),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    ThemeSpec(
        id = "theme7",
        displayName = "Indigo",
        accentPrimary = Color(0xFF3D5AFE),
        accentSecondary = Color(0xFF536DFE),
        mode1AccentColor = Color(0xFF3D5AFE),
        mode2AccentColor = Color(0xFF536DFE),
        mode3AccentColor = Color(0xFF39FF14),
        mode4AccentColor = Color(0xFFBF5AF2),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    ThemeSpec(
        id = "theme8",
        displayName = "Violet",
        accentPrimary = Color(0xFFAA00FF),
        accentSecondary = Color(0xFFD500F9),
        mode1AccentColor = Color(0xFFAA00FF),
        mode2AccentColor = Color(0xFFD500F9),
        mode3AccentColor = Color(0xFF39FF14),
        mode4AccentColor = Color(0xFFBF5AF2),
        allowLightDark = ThemeModeSupport.BOTH,
    ),
)

val DefaultTheme: ThemeSpec = AppThemes.first()

fun themeById(id: String): ThemeSpec =
    AppThemes.firstOrNull { it.id == id } ?: DefaultTheme
