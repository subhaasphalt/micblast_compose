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
        accentPrimary = Color(0xFF00E5FF),   // cyan
        accentSecondary = Color(0xFFFF2D95), // magenta
        mode1AccentColor = Color(0xFF00E5FF), // Normal   — mirrors primary
        mode2AccentColor = Color(0xFFFF2D95), // Chipmunk — mirrors secondary
        mode3AccentColor = Color(0xFF39FF14), // Monster  — fixed green
        mode4AccentColor = Color(0xFFBF5AF2), // Robot    — fixed purple
        allowLightDark = ThemeModeSupport.BOTH,
    ),
)

val DefaultTheme: ThemeSpec = AppThemes.first()

fun themeById(id: String): ThemeSpec =
    AppThemes.firstOrNull { it.id == id } ?: DefaultTheme
