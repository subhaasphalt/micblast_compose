package com.example.micblast.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

/**
 * Fully-saturated neon hues (the kind every theme here uses) read fine on
 * a near-black surface but are physically too bright to have real contrast
 * against a near-white one — cyan and green in particular are close to
 * white's own luminance. Rather than ask every theme to hand-author a
 * separate light-mode-safe variant of each of its 6 colors, we darken any
 * accent automatically when it's about to render on a light surface,
 * preserving hue/saturation so the color still reads as "the same" color,
 * just deep enough to be legible.
 *
 * 0.25 lightness was picked by checking actual WCAG contrast ratios against
 * this app's light background (#F7F7FB) — it's the point where cyan/green
 * (the worst offenders) clear 4.5:1, with magenta/purple comfortably above
 * that already. If a future theme's colors still look off after this,
 * that's a signal to tune this constant, not to special-case a theme.
 */
private const val MAX_LIGHTNESS_ON_LIGHT_SURFACE = 0.25f

fun Color.readableOnLightSurface(): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(this.toArgb(), hsl)
    if (hsl[2] <= MAX_LIGHTNESS_ON_LIGHT_SURFACE) return this
    hsl[2] = MAX_LIGHTNESS_ON_LIGHT_SURFACE
    return Color(ColorUtils.HSLToColor(hsl))
}
