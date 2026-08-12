package com.example.micblast.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * All the colors MainScreen actually reaches for. Built by merging a
 * [ThemeSpec] (the accent colors + mode-support flag) with whichever
 * shared surface palette matches the active light/dark mode. Every
 * composable downstream reads through MaterialTheme.microBlastColors, so
 * switching either the theme or the mode in Settings updates the whole
 * screen automatically.
 *
 * islandAudioSetup/islandModeGrid/their border counterparts exist so the
 * control islands (Audio Setup row, Mode Grid card) read as distinct zones
 * instead of one flat sheet — each is [Surface] tinted a little toward
 * that island's accent, derived automatically from the theme's accents
 * rather than hand-picked per theme.
 */
data class MicBlastColors(
    val bgTop: Color,
    val bgMid: Color,
    val bgBottom: Color,
    val surface: Color,
    val surfaceChip: Color,
    val borderFaint: Color,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val mode1AccentColor: Color,
    val mode2AccentColor: Color,
    val mode3AccentColor: Color,
    val mode4AccentColor: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val islandAudioSetup: Color,
    val islandAudioSetupBorder: Color,
    val islandModeGrid: Color,
    val islandModeGridBorder: Color,
    val selectedTileFillAlpha: Float,
)

/**
 * Combines a theme's accents with the shared surface palette for [darkTheme].
 * This is the one place theme + mode actually get merged. Accent colors are
 * resolved per-mode (see [ThemeSpec.resolvedAccentPrimary] etc.) so themes
 * like Monochrome can flip between a light and dark accent instead of
 * being stuck with one hex that only half-works.
 */
fun buildMicBlastColors(theme: ThemeSpec, darkTheme: Boolean): MicBlastColors {
    val surfaces = if (darkTheme) DarkSurfacePalette else LightSurfacePalette

    val accentPrimary = theme.resolvedAccentPrimary(darkTheme)
    val accentSecondary = theme.resolvedAccentSecondary(darkTheme)
    val modeAccents = theme.resolvedModeAccents(darkTheme)

    return MicBlastColors(
        bgTop = surfaces.BgTop,
        bgMid = surfaces.BgMid,
        bgBottom = surfaces.BgBottom,
        surface = surfaces.Surface,
        surfaceChip = surfaces.SurfaceChip,
        borderFaint = surfaces.BorderFaint,
        accentPrimary = accentPrimary,
        accentSecondary = accentSecondary,
        mode1AccentColor = modeAccents.getOrElse(0) { theme.mode1AccentColor },
        mode2AccentColor = modeAccents.getOrElse(1) { theme.mode2AccentColor },
        mode3AccentColor = modeAccents.getOrElse(2) { theme.mode3AccentColor },
        mode4AccentColor = modeAccents.getOrElse(3) { theme.mode4AccentColor },
        textPrimary = surfaces.TextPrimary,
        textSecondary = surfaces.TextSecondary,
        textMuted = surfaces.TextMuted,
        islandAudioSetup = lerp(surfaces.Surface, accentPrimary, theme.islandTintStrength),
        islandAudioSetupBorder = lerp(surfaces.BorderFaint, accentPrimary, theme.borderTintStrength),
        islandModeGrid = lerp(surfaces.Surface, accentSecondary, theme.islandTintStrength),
        islandModeGridBorder = lerp(surfaces.BorderFaint, accentSecondary, theme.borderTintStrength),
        selectedTileFillAlpha = theme.selectedTileFillAlpha,
    )
}

private val LocalMicBlastColors = staticCompositionLocalOf {
    buildMicBlastColors(DefaultTheme, darkTheme = false)
}

/**
 * Extension on MaterialTheme so call sites read `MaterialTheme.microBlastColors.accentPrimary`,
 * the same shape as the built-in `MaterialTheme.colorScheme`.
 */
val MaterialTheme.microBlastColors: MicBlastColors
    @Composable
    get() = LocalMicBlastColors.current

/**
 * @param theme the active [ThemeSpec] (see [AppThemes]).
 * @param darkTheme the user's requested mode. If [theme] doesn't support
 *   both modes, this is overridden internally via
 *   [ThemeModeSupport.resolveDarkTheme] — callers (MainActivity) should
 *   still resolve the effective mode themselves too, so the Settings
 *   toggle's displayed on/off state stays in sync with what's rendered.
 */
@Composable
fun MicBlastTheme(
    theme: ThemeSpec = DefaultTheme,
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val effectiveDarkTheme = theme.allowLightDark.resolveDarkTheme(darkTheme)
    val colors = buildMicBlastColors(theme, effectiveDarkTheme)

    CompositionLocalProvider(LocalMicBlastColors provides colors) {
        val scheme = if (effectiveDarkTheme) {
            darkColorScheme(
                primary = colors.accentPrimary,
                secondary = colors.accentSecondary,
                background = colors.bgTop,
                surface = colors.surface,
                onBackground = colors.textPrimary,
                onSurface = colors.textPrimary,
            )
        } else {
            lightColorScheme(
                primary = colors.accentPrimary,
                secondary = colors.accentSecondary,
                background = colors.bgTop,
                surface = colors.surface,
                onBackground = colors.textPrimary,
                onSurface = colors.textPrimary,
            )
        }

        MaterialTheme(
            colorScheme = scheme,
            content = content
        )
    }
}
