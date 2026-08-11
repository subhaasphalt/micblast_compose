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
 * A theme. The 6-color core (accentPrimary/Secondary + 4 mode colors) is
 * still the minimum any theme needs to define, and for most themes that's
 * all that's set — a single hex per role reads fine on both a near-black
 * and a near-white surface as long as it's a mid-tone (see Ocean, Berry).
 *
 * A few extra knobs exist on top of that for themes that need more than
 * "one hex per role":
 *
 *  - accentPrimary/Secondary(OverrideDark/Light): lets a theme use a
 *    genuinely different accent per mode instead of one fixed hex — needed
 *    for Monochrome, where "white on black, near-black on white" is the
 *    whole point and no single hex does both well.
 *  - mode1..4AccentColors(Dark/Light overrides): same idea, per voice-mode
 *    button, for themes where the grayscale steps need to flip too.
 *  - islandTintStrength: how much accentPrimary/Secondary bleed into the
 *    Audio Setup and Mode Grid card backgrounds, so those "control
 *    islands" read as distinct zones instead of one flat sheet. This is
 *    computed at render time (see buildMicBlastColors) — no per-theme
 *    hex-picking required, it just works off whatever accents a theme
 *    already defines.
 *  - borderTintStrength: same idea for the island borders.
 *  - selectedTileFillAlpha: how strongly a selected mode tile's background
 *    is tinted by its own mode color.
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
    val accentPrimaryOverrideDark: Color? = null,
    val accentPrimaryOverrideLight: Color? = null,
    val accentSecondaryOverrideDark: Color? = null,
    val accentSecondaryOverrideLight: Color? = null,
    val modeAccentColorsDark: List<Color>? = null,
    val modeAccentColorsLight: List<Color>? = null,
    val islandTintStrength: Float = 0.07f,
    val borderTintStrength: Float = 0.4f,
    val selectedTileFillAlpha: Float = 0.16f,
) {
    fun resolvedAccentPrimary(darkTheme: Boolean): Color =
        (if (darkTheme) accentPrimaryOverrideDark else accentPrimaryOverrideLight) ?: accentPrimary

    fun resolvedAccentSecondary(darkTheme: Boolean): Color =
        (if (darkTheme) accentSecondaryOverrideDark else accentSecondaryOverrideLight) ?: accentSecondary

    fun resolvedModeAccents(darkTheme: Boolean): List<Color> =
        (if (darkTheme) modeAccentColorsDark else modeAccentColorsLight)
            ?: listOf(mode1AccentColor, mode2AccentColor, mode3AccentColor, mode4AccentColor)
}

/**
 * The theme registry. This is the single source of truth for what themes
 * exist in the app — add a new [ThemeSpec] entry here and it automatically
 * shows up in Settings (see SettingsScreen's theme picker) with no other
 * wiring required.
 *
 * Every theme here sticks to analogous or single-hue color relationships —
 * no complementary/split-complementary pairs, and no four mode-button
 * colors thrown together from unrelated hues. That combination reads as
 * "chaotic rainbow" in practice; a tight hue range reads as "designed."
 */
val AppThemes: List<ThemeSpec> = listOf(

    // 1. Monochrome — true black/white, flips per mode via overrides so it's
    // never washed out. The one theme that needs the override mechanism.
    ThemeSpec(
        id = "monochrome",
        displayName = "Monochrome",
        accentPrimary = Color(0xFFFFFFFF),
        accentSecondary = Color(0xFFD6D6D6),
        mode1AccentColor = Color(0xFFECECEC),
        mode2AccentColor = Color(0xFFBFBFBF),
        mode3AccentColor = Color(0xFF939393),
        mode4AccentColor = Color(0xFF6B6B6B),
        allowLightDark = ThemeModeSupport.BOTH,
        accentPrimaryOverrideDark = Color(0xFFFFFFFF),
        accentPrimaryOverrideLight = Color(0xFF1C1C1E),
        accentSecondaryOverrideDark = Color(0xFFD6D6D6),
        accentSecondaryOverrideLight = Color(0xFF48484A),
        modeAccentColorsDark = listOf(
            Color(0xFFECECEC), Color(0xFFBFBFBF), Color(0xFF939393), Color(0xFF6B6B6B)
        ),
        modeAccentColorsLight = listOf(
            Color(0xFF2E2E2E), Color(0xFF4F4F4F), Color(0xFF6E6E6E), Color(0xFF8C8C8C)
        ),
        islandTintStrength = 0.045f,
        borderTintStrength = 0.55f,
    ),

    // 2. Pastel Pop — soft but saturated enough to pop, per your feedback.
    // Cool arc: mint -> sky -> violet -> orchid, all analogous.
    ThemeSpec(
        id = "pastel_pop",
        displayName = "Pastel Pop",
        accentPrimary = Color(0xFF9B7FE0),
        accentSecondary = Color(0xFF5FB8E8),
        mode1AccentColor = Color(0xFF5FCFC0),
        mode2AccentColor = Color(0xFF5FB8E8),
        mode3AccentColor = Color(0xFF9B7FE0),
        mode4AccentColor = Color(0xFFE27FC2),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // 3. Ocean — analogous blue/teal
    ThemeSpec(
        id = "ocean",
        displayName = "Ocean",
        accentPrimary = Color(0xFF0077B6),
        accentSecondary = Color(0xFF00B4D8),
        mode1AccentColor = Color(0xFF0077B6),
        mode2AccentColor = Color(0xFF00B4D8),
        mode3AccentColor = Color(0xFF48CAE4),
        mode4AccentColor = Color(0xFF2E5EAA),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // 4. Forest — analogous green, no amber outlier this time
    ThemeSpec(
        id = "forest",
        displayName = "Forest",
        accentPrimary = Color(0xFF1B8A5A),
        accentSecondary = Color(0xFF6FBE44),
        mode1AccentColor = Color(0xFF1B8A5A),
        mode2AccentColor = Color(0xFF6FBE44),
        mode3AccentColor = Color(0xFF2FBF8F),
        mode4AccentColor = Color(0xFF4E9A3A),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // 5. Sunset — warm analogous orange -> coral -> pink
    ThemeSpec(
        id = "sunset",
        displayName = "Sunset",
        accentPrimary = Color(0xFFFF6B35),
        accentSecondary = Color(0xFFFF3B5C),
        mode1AccentColor = Color(0xFFFF6B35),
        mode2AccentColor = Color(0xFFFF3B5C),
        mode3AccentColor = Color(0xFFFF9640),
        mode4AccentColor = Color(0xFFFF6FA5),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // 6. Berry — analogous magenta/purple
    ThemeSpec(
        id = "berry",
        displayName = "Berry",
        accentPrimary = Color(0xFFD81B60),
        accentSecondary = Color(0xFF8E24AA),
        mode1AccentColor = Color(0xFFD81B60),
        mode2AccentColor = Color(0xFF8E24AA),
        mode3AccentColor = Color(0xFFE91E8C),
        mode4AccentColor = Color(0xFFB03BC4),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // 7. Slate — cool blue-gray, understated/professional
    ThemeSpec(
        id = "slate",
        displayName = "Slate",
        accentPrimary = Color(0xFF5C7A99),
        accentSecondary = Color(0xFF8FA6BD),
        mode1AccentColor = Color(0xFF5C7A99),
        mode2AccentColor = Color(0xFF8FA6BD),
        mode3AccentColor = Color(0xFF4A6580),
        mode4AccentColor = Color(0xFFA8BBCB),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // 8. Amber Gold — warm single-hue luxury steps, gold -> bronze
    ThemeSpec(
        id = "amber_gold",
        displayName = "Amber Gold",
        accentPrimary = Color(0xFFD4A93E),
        accentSecondary = Color(0xFFE8C978),
        mode1AccentColor = Color(0xFFD4A93E),
        mode2AccentColor = Color(0xFFE8C978),
        mode3AccentColor = Color(0xFFB8862A),
        mode4AccentColor = Color(0xFFC99A50),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // 9. Rosewood — muted mauve/pink, soft and sophisticated
    ThemeSpec(
        id = "rosewood",
        displayName = "Rosewood",
        accentPrimary = Color(0xFFB5707D),
        accentSecondary = Color(0xFFD9A6AE),
        mode1AccentColor = Color(0xFFB5707D),
        mode2AccentColor = Color(0xFFD9A6AE),
        mode3AccentColor = Color(0xFF8F4E5C),
        mode4AccentColor = Color(0xFFC98793),
        allowLightDark = ThemeModeSupport.BOTH,
    ),

    // 10. Ember — deep warm red-orange
    ThemeSpec(
        id = "ember",
        displayName = "Ember",
        accentPrimary = Color(0xFFE8503A),
        accentSecondary = Color(0xFFF3894A),
        mode1AccentColor = Color(0xFFE8503A),
        mode2AccentColor = Color(0xFFF3894A),
        mode3AccentColor = Color(0xFFC43A2E),
        mode4AccentColor = Color(0xFFF7B267),
        allowLightDark = ThemeModeSupport.BOTH,
    ),
)

val DefaultTheme: ThemeSpec = AppThemes.first()

fun themeById(id: String): ThemeSpec =
    AppThemes.firstOrNull { it.id == id } ?: DefaultTheme
