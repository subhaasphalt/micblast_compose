package com.example.micblast.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.micblast.R
import com.example.micblast.ui.theme.ThemeModeSupport
import com.example.micblast.ui.theme.ThemeSpec
import com.example.micblast.ui.theme.microBlastColors

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
            // The theme grid can grow to any length (one row per 2 themes),
            // and the toggle cards still need to be reachable below it —
            // without this the screen just clips once themes overflow.
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { hapticClick(); onBack() },
                modifier = Modifier
                    .size(44.dp)
                    .background(colors.surface, RoundedCornerShape(12.dp))
                    .border(1.5.dp, colors.accentPrimary.copy(alpha = 0.78f), RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_button_cd),
                    tint = colors.accentPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    brush = Brush.linearGradient(
                        colors = listOf(colors.accentPrimary, colors.accentSecondary)
                    )
                ),
                fontSize = 17.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            // Balances the back button so the title stays visually centered.
            Spacer(modifier = Modifier.size(44.dp))
        }

        Spacer(modifier = Modifier.height(28.dp))

        ThemePickerSection(
            themes = themes,
            selectedThemeId = selectedThemeId,
            onThemeSelected = { hapticClick(); onThemeSelected(it) },
        )

        Spacer(modifier = Modifier.height(14.dp))

        val darkModeDescRes = when {
            !darkModeToggleEnabled && darkTheme -> R.string.dark_mode_locked_dark_desc
            !darkModeToggleEnabled && !darkTheme -> R.string.dark_mode_locked_light_desc
            darkTheme -> R.string.dark_mode_on_desc
            else -> R.string.dark_mode_off_desc
        }

        SettingsToggleCard(
            label = stringResource(R.string.dark_mode_label),
            description = stringResource(darkModeDescRes),
            checked = darkTheme,
            enabled = darkModeToggleEnabled,
            onCheckedChange = { hapticClick(); onDarkThemeChange(it) },
        )

        Spacer(modifier = Modifier.height(14.dp))

        SettingsToggleCard(
            label = stringResource(R.string.auto_rotate_label),
            description = stringResource(
                if (autoRotate) R.string.auto_rotate_on_desc else R.string.auto_rotate_off_desc
            ),
            checked = autoRotate,
            onCheckedChange = { hapticClick(); onAutoRotateChange(it) },
        )

        Spacer(modifier = Modifier.height(14.dp))

        SettingsToggleCard(
            label = stringResource(R.string.haptic_feedback_label),
            description = stringResource(
                if (hapticFeedback) R.string.haptic_feedback_on_desc else R.string.haptic_feedback_off_desc
            ),
            checked = hapticFeedback,
            onCheckedChange = { hapticClick(); onHapticFeedbackChange(it) },
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Lays out every theme in [themes] (see AppThemes — add an entry there and
 * it shows up here automatically) as a 2-per-row grid of [ThemeTile]s, so
 * the list stays compact as more themes get added instead of growing one
 * full-width row per theme.
 */
@Composable
private fun ThemePickerSection(
    themes: List<ThemeSpec>,
    selectedThemeId: String,
    onThemeSelected: (String) -> Unit,
) {
    val colors = MaterialTheme.microBlastColors

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.theme_label),
            color = colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        themes.chunked(2).forEach { rowThemes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowThemes.forEach { theme ->
                    ThemeTile(
                        theme = theme,
                        selected = theme.id == selectedThemeId,
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
 * One theme's preview tile, kept deliberately minimal: a left accent bar in
 * the theme's main color, the 4 voice-mode button colors as circles, the
 * theme name with its supported modes in a smaller/muted caption below it,
 * and — only when this theme is the active one — a small green tick badge
 * in the top-right corner. No borders or pills competing for attention;
 * the accent bar and mode circles ARE the preview.
 */
@Composable
private fun ThemeTile(
    theme: ThemeSpec,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.microBlastColors
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(colors.surfaceChip)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) theme.accentPrimary.copy(alpha = 0.55f) else colors.borderFaint,
                shape = shape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left accent bar: the theme's main color, full tile height.
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(theme.accentPrimary)
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
                    listOf(
                        theme.mode1AccentColor,
                        theme.mode2AccentColor,
                        theme.mode3AccentColor,
                        theme.mode4AccentColor,
                    ).forEach { modeColor ->
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(modeColor)
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

@Composable
private fun SettingsToggleCard(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.microBlastColors

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, if (checked) colors.accentPrimary.copy(alpha = 0.34f) else colors.borderFaint),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.55f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.accentPrimary,
                    checkedTrackColor = colors.accentPrimary.copy(alpha = 0.4f),
                    uncheckedThumbColor = colors.textSecondary,
                    uncheckedTrackColor = colors.borderFaint,
                )
            )
        }
    }
}
