package com.example.micblast.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * All the colors MainScreen actually reaches for. This is the seam for
 * Settings-driven theming later: build a different MicBlastColors instance
 * (or several presets) and pass it into MicBlastTheme — every composable
 * downstream reads through MaterialTheme.microBlastColors, so nothing in
 * MainScreen.kt needs to change when a theme picker is added.
 */
data class MicBlastColors(
    val bgTop: Color,
    val bgBottom: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val borderFaint: Color,
    val accentCyan: Color,
    val accentMagenta: Color,
    val accentGreen: Color,
    val accentPurple: Color,
    val textPrimary: Color,
    val textSecondary: Color,
)

val NeonColors = MicBlastColors(
    bgTop = NeonPalette.BgTop,
    bgBottom = NeonPalette.BgBottom,
    surface = NeonPalette.Surface,
    surfaceAlt = NeonPalette.SurfaceAlt,
    borderFaint = NeonPalette.BorderFaint,
    accentCyan = NeonPalette.Cyan,
    accentMagenta = NeonPalette.Magenta,
    accentGreen = NeonPalette.Green,
    accentPurple = NeonPalette.Purple,
    textPrimary = NeonPalette.TextPrimary,
    textSecondary = NeonPalette.TextSecondary,
)

private val LocalMicBlastColors = staticCompositionLocalOf { NeonColors }

/**
 * Extension on MaterialTheme so call sites read `MaterialTheme.microBlastColors.accentCyan`,
 * the same shape as the built-in `MaterialTheme.colorScheme`.
 */
val MaterialTheme.microBlastColors: MicBlastColors
    @Composable
    get() = LocalMicBlastColors.current

@Composable
fun MicBlastTheme(
    colors: MicBlastColors = NeonColors,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalMicBlastColors provides colors) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = colors.accentCyan,
                secondary = colors.accentMagenta,
                background = colors.bgTop,
                surface = colors.surface,
                onBackground = colors.textPrimary,
                onSurface = colors.textPrimary,
            ),
            content = content
        )
    }
}
