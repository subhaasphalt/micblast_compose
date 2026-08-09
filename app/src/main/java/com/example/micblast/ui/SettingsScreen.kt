package com.example.micblast.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

        ThemePickerCard(
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
    }
}

/**
 * Lists every theme in [themes] (see AppThemes — add an entry there and it
 * shows up here automatically) as a selectable row with a little swatch
 * previewing its primary/secondary pair.
 */
@Composable
private fun ThemePickerCard(
    themes: List<ThemeSpec>,
    selectedThemeId: String,
    onThemeSelected: (String) -> Unit,
) {
    val colors = MaterialTheme.microBlastColors

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.borderFaint),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.theme_label),
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            themes.forEachIndexed { index, theme ->
                ThemeRow(
                    theme = theme,
                    selected = theme.id == selectedThemeId,
                    onClick = { onThemeSelected(theme.id) },
                )
                if (index != themes.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun ThemeRow(
    theme: ThemeSpec,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.microBlastColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) colors.accentPrimary.copy(alpha = 0.12f) else colors.surfaceChip)
            .border(
                1.dp,
                if (selected) colors.accentPrimary.copy(alpha = 0.55f) else colors.borderFaint,
                RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(theme.accentPrimary)
            )
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(theme.accentSecondary)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = theme.displayName,
                color = colors.textPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            if (theme.allowLightDark != ThemeModeSupport.BOTH) {
                Text(
                    text = if (theme.allowLightDark == ThemeModeSupport.DARK_ONLY) "Dark only" else "Light only",
                    color = colors.textMuted,
                    fontSize = 11.sp
                )
            }
        }

        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = colors.accentPrimary,
                modifier = Modifier.size(20.dp)
            )
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
