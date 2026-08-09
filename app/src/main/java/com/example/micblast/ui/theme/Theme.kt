package com.example.micblast.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * All the colors MainScreen actually reaches for. Built by merging a
 * [ThemeSpec] (the 6 accent colors + mode-support flag) with whichever
 * shared surface palette matches the active light/dark mode. Every
 * composable downstream reads through MaterialTheme.microBlastColors, so
 * switching either the theme or the mode in Settings updates the whole
 * screen automatically.
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
)

/**
 * Combines a theme's accents with the shared surface palette for [darkTheme].
 * This is the one place theme + mode actually get merged.
 */
fun buildMicBlastColors(theme: ThemeSpec, darkTheme: Boolean): MicBlastColors {
    val surfaces = if (darkTheme) DarkSurfacePalette else LightSurfacePalette
    return MicBlastColors(
        bgTop = surfaces.BgTop,
        bgMid = surfaces.BgMid,
        bgBottom = surfaces.BgBottom,
        surface = surfaces.Surface,
        surfaceChip = surfaces.SurfaceChip,
        borderFaint = surfaces.BorderFaint,
        accentPrimary = theme.accentPrimary,
        accentSecondary = theme.accentSecondary,
        mode1AccentColor = theme.mode1AccentColor,
        mode2AccentColor = theme.mode2AccentColor,
        mode3AccentColor = theme.mode3AccentColor,
        mode4AccentColor = theme.mode4AccentColor,
        textPrimary = surfaces.TextPrimary,
        textSecondary = surfaces.TextSecondary,
        textMuted = surfaces.TextMuted,
    )
}

private val LocalMicBlastColors = staticCompositionLocalOf {
    buildMicBlastColors(DefaultTheme, darkTheme = true)
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
    darkTheme: Boolean = true,
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
