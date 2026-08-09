package com.example.micblast.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * All the colors MainScreen actually reaches for. Two instances exist —
 * NeonColors (dark) and LightColors — and MicBlastTheme picks between them
 * based on the darkTheme flag. Every composable downstream reads through
 * MaterialTheme.microBlastColors, so switching themes in Settings updates
 * the whole screen automatically.
 */
data class MicBlastColors(
    val bgTop: Color,
    val bgMid: Color,
    val bgBottom: Color,
    val surface: Color,
    val surfaceChip: Color,
    val borderFaint: Color,
    val accentCyan: Color,
    val accentMagenta: Color,
    val accentGreen: Color,
    val accentPurple: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
)

val NeonColors = MicBlastColors(
    bgTop = NeonPalette.BgTop,
    bgMid = NeonPalette.BgMid,
    bgBottom = NeonPalette.BgBottom,
    surface = NeonPalette.Surface,
    surfaceChip = NeonPalette.SurfaceChip,
    borderFaint = NeonPalette.BorderFaint,
    accentCyan = NeonPalette.Cyan,
    accentMagenta = NeonPalette.Magenta,
    accentGreen = NeonPalette.Green,
    accentPurple = NeonPalette.Purple,
    textPrimary = NeonPalette.TextPrimary,
    textSecondary = NeonPalette.TextSecondary,
    textMuted = NeonPalette.TextMuted,
)

val LightColors = MicBlastColors(
    bgTop = LightPalette.BgTop,
    bgMid = LightPalette.BgMid,
    bgBottom = LightPalette.BgBottom,
    surface = LightPalette.Surface,
    surfaceChip = LightPalette.SurfaceChip,
    borderFaint = LightPalette.BorderFaint,
    accentCyan = LightPalette.Cyan,
    accentMagenta = LightPalette.Magenta,
    accentGreen = LightPalette.Green,
    accentPurple = LightPalette.Purple,
    textPrimary = LightPalette.TextPrimary,
    textSecondary = LightPalette.TextSecondary,
    textMuted = LightPalette.TextMuted,
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
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) NeonColors else LightColors

    CompositionLocalProvider(LocalMicBlastColors provides colors) {
        val scheme = if (darkTheme) {
            darkColorScheme(
                primary = colors.accentCyan,
                secondary = colors.accentMagenta,
                background = colors.bgTop,
                surface = colors.surface,
                onBackground = colors.textPrimary,
                onSurface = colors.textPrimary,
            )
        } else {
            lightColorScheme(
                primary = colors.accentCyan,
                secondary = colors.accentMagenta,
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
