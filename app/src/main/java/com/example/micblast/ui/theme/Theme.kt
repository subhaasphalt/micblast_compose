package com.example.micblast.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * All the colors MainScreen reaches for. Built from dark/light base +
 * the selected accent pair via [buildColors].
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

private val LocalMicBlastColors = staticCompositionLocalOf {
    buildColors(darkTheme = true, accent = AccentTheme.CLASSIC)
}

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
    accentTheme: AccentTheme = AccentTheme.CLASSIC,
    content: @Composable () -> Unit
) {
    val colors = buildColors(darkTheme, accentTheme)

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
