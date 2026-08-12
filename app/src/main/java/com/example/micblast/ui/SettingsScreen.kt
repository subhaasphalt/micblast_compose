package com.example.micblast.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ScreenLockRotation
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.micblast.R
import com.example.micblast.ui.theme.ThemeModeSupport
import com.example.micblast.ui.theme.ThemeSpec
import com.example.micblast.ui.theme.microBlastColors

/**
 * Settings, fully collapsed into two pieces:
 *  - [QuickSettingsIsland]: back button + dark mode / auto-rotate / haptics,
 *    anchored at the top and never scrolls away. Each toggle is a single
 *    icon that swaps to a different glyph per state (sun/moon,
 *    rotate-arrows/locked, vibrate-waves/plain) instead of an icon+label+
 *    switch trio — the icon *is* the state.
 *  - the theme tiles, which scroll underneath. No "Theme" header — the
 *    tiles start immediately below the island.
 */
@Composable
fun SettingsScreen(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    darkModeToggleEnabled: Boolean,
    themes: List<ThemeSpec>,
    selectedThemeId: String,
    onThemeSelected: (String) -> Unit,
    autoRotate: Boolean,
    onAutoRotateChange: (Boolean) -> Unit,
    hapticFeedback: Boolean,
    onHapticFeedbackChange: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val colors = MaterialTheme.microBlastColors
    val hapticClick = rememberHapticClick(hapticFeedback)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(colors.bgTop, colors.bgMid, colors.bgBottom)
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        QuickSettingsIsland(
            darkTheme = darkTheme,
            darkModeToggleEnabled = darkModeToggleEnabled,
            onDarkThemeChange = { hapticClick(); onDarkThemeChange(it) },
            autoRotate = autoRotate,
            onAutoRotateChange = { hapticClick(); onAutoRotateChange(it) },
            hapticFeedback = hapticFeedback,
            onHapticFeedbackChange = { hapticClick(); onHapticFeedbackChange(it) },
            onBack = { hapticClick(); onBack() },
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Only the tiles scroll — the island above stays put regardless of
        // how many themes end up in the list.
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            ThemeTileGrid(
                themes = themes,
                selectedThemeId = selectedThemeId,
                darkTheme = darkTheme,
                onThemeSelected = { hapticClick(); onThemeSelected(it) },
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * The anchored back-button + 3-toggle bar. Each toggle is one tap target:
 * tapping it flips the underlying boolean directly (no separate switch to
 * hit), and its own glyph + a light accent-tinted ring communicate current
 * state — geometry AND color, so state doesn't rely on spotting a subtle
 * icon swap alone at a glance.
 */
@Composable
private fun QuickSettingsIsland(
    darkTheme: Boolean,
    darkModeToggleEnabled: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    autoRotate: Boolean,
    onAutoRotateChange: (Boolean) -> Unit,
    hapticFeedback: Boolean,
    onHapticFeedbackChange: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val colors = MaterialTheme.microBlastColors
    val shape = TopBarIslandShape

    val darkModeDescRes = when {
        !darkModeToggleEnabled && darkTheme -> R.string.dark_mode_locked_dark_desc
        !darkModeToggleEnabled && !darkTheme -> R.string.dark_mode_locked_light_desc
        darkTheme -> R.string.dark_mode_on_desc
        else -> R.string.dark_mode_off_desc
    }

    // No dividers between the buttons, and all four (back + 3 toggles) are
    // the same fixed 40dp size — Arrangement.SpaceEvenly then gives them
    // identical gaps instead of the back button sitting tight against the
    // edge while the toggles stretch to fill whatever weighted space is
    // left over.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.borderFaint, shape)
            .padding(horizontal = TopBarIslandPadding, vertical = TopBarIslandPadding),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Same TopBarIconButton used for menu/lock on MainScreen — same
        // size, fill, and ring geometry, just a neutral tint since back
        // isn't a themed accent action.
        TopBarIconButton(
            icon = Icons.Filled.ArrowBack,
            contentDescription = stringResource(R.string.back_button_cd),
            tint = colors.textPrimary,
            onClick = onBack,
        )

        QuickToggle(
            icon = if (darkTheme) Icons.Filled.Brightness4 else Icons.Filled.WbSunny,
            contentDescription = "${stringResource(R.string.dark_mode_label)}: ${stringResource(darkModeDescRes)}",
            checked = darkTheme,
            enabled = darkModeToggleEnabled,
            onToggle = { onDarkThemeChange(!darkTheme) },
        )

        QuickToggle(
            icon = if (autoRotate) Icons.Filled.ScreenRotation else Icons.Filled.ScreenLockRotation,
            contentDescription = "${stringResource(R.string.auto_rotate_label)}: ${
                stringResource(if (autoRotate) R.string.auto_rotate_on_desc else R.string.auto_rotate_off_desc)
            }",
            checked = autoRotate,
            onToggle = { onAutoRotateChange(!autoRotate) },
        )

        QuickToggle(
            icon = if (hapticFeedback) Icons.Filled.Vibration else Icons.Filled.StayCurrentPortrait,
            contentDescription = "${stringResource(R.string.haptic_feedback_label)}: ${
                stringResource(if (hapticFeedback) R.string.haptic_feedback_on_desc else R.string.haptic_feedback_off_desc)
            }",
            checked = hapticFeedback,
            onToggle = { onHapticFeedbackChange(!hapticFeedback) },
        )
    }
}

/**
 * One icon-only toggle. On: accent-tinted fill + ring, "on" glyph. Off:
 * neutral surfaceChip fill, faint ring, "off" glyph. Same visual language
 * as the mode-select tiles on the main screen, just circular.
 */
@Composable
private fun QuickToggle(
    icon: ImageVector,
    contentDescription: String,
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.microBlastColors
    val background = if (checked) colors.accentPrimary.copy(alpha = 0.16f) else colors.surfaceChip
    val ringColor = if (checked) colors.accentPrimary else colors.borderFaint
    val iconTint = if (checked) colors.accentPrimary else colors.textSecondary

    Box(
        modifier = modifier.alpha(if (enabled) 1f else 0.45f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(background)
                .border(1.dp, ringColor, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = enabled,
                    onClick = onToggle,
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Lays out every theme in [themes] (see AppThemes — add an entry there and
 * it shows up here automatically) as a 2-per-row grid of [ThemeTile]s, so
 * the list stays compact as more themes get added instead of growing one
 * full-width row per theme.
 */
@Composable
private fun ThemeTileGrid(
    themes: List<ThemeSpec>,
    selectedThemeId: String,
    darkTheme: Boolean,
    onThemeSelected: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        themes.chunked(2).forEach { rowThemes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowThemes.forEach { theme ->
                    ThemeTile(
                        theme = theme,
                        selected = theme.id == selectedThemeId,
                        darkTheme = darkTheme,
                        onClick = { onThemeSelected(theme.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Odd theme count: keep the last tile at half width instead
                // of stretching it across the full row.
                if (rowThemes.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

/** Small solid green used only for the "this theme is active" badge. */
private val ActiveIndicatorGreen = Color(0xFF34C759)

/**
 * One theme's preview tile, kept deliberately minimal: a left accent bar
 * gradienting between the theme's 2 accent colors, the 4 voice-mode button colors as circles, the
 * theme name with its supported modes in a smaller/muted caption below it,
 * and — only when this theme is the active one — a small green tick badge
 * in the top-right corner. No borders or pills competing for attention;
 * the accent bar and mode circles ARE the preview.
 *
 * Colors are resolved against this theme's own *effective* mode (its
 * light/dark override, if any, given the app's current dark-mode request)
 * rather than its raw authored defaults — otherwise a theme like
 * Monochrome, whose accents flip between modes, would always preview its
 * dark-mode colors even while the picker itself is in light mode, which is
 * how a near-white accent circle ends up nearly invisible on a light tile.
 */
@Composable
private fun ThemeTile(
    theme: ThemeSpec,
    selected: Boolean,
    darkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.microBlastColors
    val shape = RoundedCornerShape(16.dp)
    val effectiveDark = theme.allowLightDark.resolveDarkTheme(darkTheme)
    val previewAccentPrimary = theme.resolvedAccentPrimary(effectiveDark)
    val previewAccentSecondary = theme.resolvedAccentSecondary(effectiveDark)
    val previewModeAccents = theme.resolvedModeAccents(effectiveDark)

    Box(
        modifier = modifier
            .clip(shape)
            .background(colors.surfaceChip)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) previewAccentPrimary.copy(alpha = 0.55f) else colors.borderFaint,
                shape = shape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left accent bar: gradient of the theme's 2 accent colors, full tile height.
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(previewAccentPrimary, previewAccentSecondary)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 10.dp, top = 10.dp, bottom = 10.dp)
            ) {
                Text(
                    text = theme.displayName,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    lineHeight = 17.sp,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = when (theme.allowLightDark) {
                        ThemeModeSupport.BOTH -> "Light & Dark"
                        ThemeModeSupport.DARK_ONLY -> "Dark only"
                        ThemeModeSupport.LIGHT_ONLY -> "Light only"
                    },
                    color = colors.textMuted,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    lineHeight = 12.sp,
                    maxLines = 1,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    previewModeAccents.forEach { modeColor ->
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(modeColor)
                                // Faint neutral ring so every circle stays
                                // legible even when a theme's accent sits
                                // close in value to the tile background —
                                // this doesn't depend on knowing the accent
                                // color in advance, so it holds for any
                                // theme, not just ones we've eyeballed.
                                .border(1.dp, colors.borderFaint, CircleShape)
                        )
                    }
                }
            }
        }

        // Active-theme badge: small green tick, top-right corner only.
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(15.dp)
                    .clip(CircleShape)
                    .background(ActiveIndicatorGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = stringResource(R.string.theme_active_cd),
                    tint = Color.White,
                    modifier = Modifier.size(9.dp)
                )
            }
        }
    }
}
