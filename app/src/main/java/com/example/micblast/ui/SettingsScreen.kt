package com.example.micblast.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.micblast.R
import com.example.micblast.ui.theme.AccentTheme
import com.example.micblast.ui.theme.microBlastColors

@Composable
fun SettingsScreen(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    accentTheme: AccentTheme,
    onAccentThemeChange: (AccentTheme) -> Unit,
    autoRotate: Boolean,
    onAutoRotateChange: (Boolean) -> Unit,
    hapticFeedback: Boolean,
    onHapticFeedbackChange: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val colors = MaterialTheme.microBlastColors
    val hapticClick = rememberHapticClick(hapticFeedback)
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(colors.bgTop, colors.bgMid, colors.bgBottom)
                )
            )
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { hapticClick(); onBack() },
                modifier = Modifier
                    .size(44.dp)
                    .background(colors.surface, RoundedCornerShape(12.dp))
                    .border(1.5.dp, colors.accentCyan.copy(alpha = 0.78f), RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_button_cd),
                    tint = colors.accentCyan,
                    modifier = Modifier.size(26.dp)
                )
            }

            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    brush = Brush.linearGradient(
                        colors = listOf(colors.accentCyan, colors.accentMagenta)
                    )
                ),
                fontSize = 17.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.size(44.dp))
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ----- Appearance -----
        Text(
            text = stringResource(R.string.appearance_section_label),
            color = colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
        )

        SettingsToggleCard(
            label = stringResource(R.string.dark_mode_label),
            description = stringResource(
                if (darkTheme) R.string.dark_mode_on_desc else R.string.dark_mode_off_desc
            ),
            checked = darkTheme,
            onCheckedChange = { hapticClick(); onDarkThemeChange(it) },
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ----- Accent pairs -----
        Text(
            text = stringResource(R.string.accent_section_label),
            color = colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
        )

        val themes = AccentTheme.entries
        themes.chunked(2).forEach { rowThemes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowThemes.forEach { theme ->
                    AccentPreviewCard(
                        theme = theme,
                        isSelected = theme == accentTheme,
                        onClick = {
                            hapticClick()
                            onAccentThemeChange(theme)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowThemes.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ----- Behavior -----
        Text(
            text = stringResource(R.string.behavior_section_label),
            color = colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
        )

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

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AccentPreviewCard(
    theme: AccentTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentColors = MaterialTheme.microBlastColors

    Card(
        colors = CardDefaults.cardColors(containerColor = currentColors.surface),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) currentColors.accentCyan else currentColors.borderFaint
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Horizontal gradient strip showing the pair
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(theme.start, theme.end)
                        )
                    )
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = theme.displayName,
                color = currentColors.textPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
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
) {
    val colors = MaterialTheme.microBlastColors

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, if (checked) colors.accentCyan.copy(alpha = 0.34f) else colors.borderFaint),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
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
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.accentCyan,
                    checkedTrackColor = colors.accentCyan.copy(alpha = 0.4f),
                    uncheckedThumbColor = colors.textSecondary,
                    uncheckedTrackColor = colors.borderFaint,
                )
            )
        }
    }
}
