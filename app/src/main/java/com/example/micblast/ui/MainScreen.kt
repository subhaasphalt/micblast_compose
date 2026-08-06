package com.example.micblast.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.micblast.AudioLoopbackService
import com.example.micblast.R
import com.example.micblast.ui.theme.microBlastColors
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private data class VoiceModeUi(val id: String, val label: String, val color: Color)

/**
 * Stateless top-level screen: every value comes in as a parameter and every
 * user action goes out through a callback. MainActivity owns the actual
 * state (and the service calls that follow from it) — this file is purely
 * layout and look, which is the point of the Compose/theming split.
 */
@Composable
fun MainScreen(
    isRunning: Boolean,
    currentMode: String,
    gainProgress: Int, // 0-100, maps to 1.0x-2.0x
    intensityProgress: Int, // 0-100
    audioSetupIndex: Int,
    audioSetupLabels: List<String>,
    isLocked: Boolean,
    orientationLocked: Boolean,
    onPlayClick: () -> Unit,
    onStopClick: () -> Unit,
    onModeSelect: (String) -> Unit,
    onGainChange: (Int) -> Unit,
    onIntensityChange: (Int) -> Unit,
    onAudioSetupSelect: (Int) -> Unit,
    onLockClick: () -> Unit,
    onUnlock: () -> Unit,
    onOrientationLockToggle: () -> Unit,
    onMenuClick: () -> Unit,
) {
    val colors = MaterialTheme.microBlastColors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.bgTop, colors.bgBottom))),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
        ) {
            TopBar(onMenuClick = onMenuClick, onLockClick = onLockClick)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp),
                contentAlignment = Alignment.Center,
            ) {
                PrimaryActionButton(isRunning = isRunning, onPlayClick = onPlayClick, onStopClick = onStopClick)
            }

            AudioSetupDropdown(
                selectedIndex = audioSetupIndex,
                labels = audioSetupLabels,
                onSelect = onAudioSetupSelect,
            )

            VSpace(22.dp)

            LoudnessSection(progress = gainProgress, onProgressChange = onGainChange)

            VSpace(22.dp)

            ModeGrid(currentMode = currentMode, onModeSelect = onModeSelect)

            VSpace(20.dp)

            IntensitySection(
                progress = intensityProgress,
                enabled = currentMode != AudioLoopbackService.MODE_NORMAL,
                onProgressChange = onIntensityChange,
            )
        }

        OrientationLockFab(
            locked = orientationLocked,
            onToggle = onOrientationLockToggle,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
        )

        if (isLocked) {
            LockOverlay(onUnlock = onUnlock)
        }
    }
}

@Composable
private fun VSpace(height: Dp) {
    Box(modifier = Modifier.height(height))
}

@Composable
private fun TopBar(onMenuClick: () -> Unit, onLockClick: () -> Unit) {
    val colors = MaterialTheme.microBlastColors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .size(40.dp)
                .border(1.dp, colors.borderFaint, RoundedCornerShape(10.dp)),
        ) {
            Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.menu_button_cd), tint = colors.accentCyan)
        }

        Text(
            text = stringResource(R.string.app_title),
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )

        IconButton(
            onClick = onLockClick,
            modifier = Modifier
                .size(44.dp)
                .border(1.dp, colors.accentMagenta, CircleShape),
        ) {
            Icon(Icons.Filled.Lock, contentDescription = stringResource(R.string.lock_button_cd), tint = colors.accentMagenta)
        }
    }
}

@Composable
private fun PrimaryActionButton(isRunning: Boolean, onPlayClick: () -> Unit, onStopClick: () -> Unit) {
    val colors = MaterialTheme.microBlastColors
    // Play and Stop swap in the same spot rather than sitting side by side —
    // the Compose equivalent of the old cross-fade + scale ViewPropertyAnimator
    // pair in MainActivity's animatePrimaryAction().
    AnimatedContent(
        targetState = isRunning,
        transitionSpec = {
            scaleIn(tween(260), initialScale = 0.6f) togetherWith scaleOut(tween(200), targetScale = 0.6f)
        },
        label = "playStopMorph",
    ) { running ->
        val diameter = if (running) 84.dp else 96.dp
        val tint = if (running) colors.accentMagenta else colors.accentCyan
        Box(
            modifier = Modifier
                .size(diameter)
                .clip(CircleShape)
                .background(colors.surfaceAlt)
                .border(2.dp, tint, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(onClick = if (running) onStopClick else onPlayClick) {
                Icon(
                    imageVector = if (running) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(36.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AudioSetupDropdown(selectedIndex: Int, labels: List<String>, onSelect: (Int) -> Unit) {
    val colors = MaterialTheme.microBlastColors
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = labels.getOrElse(selectedIndex) { labels.firstOrNull().orEmpty() }

    Column {
        Text(
            text = stringResource(R.string.audio_setup_label),
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            TextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedTextColor = colors.textSecondary,
                    unfocusedTextColor = colors.textSecondary,
                    focusedIndicatorColor = colors.borderFaint,
                    unfocusedIndicatorColor = colors.borderFaint,
                ),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                labels.forEachIndexed { index, label ->
                    DropdownMenuItem(
                        text = { Text(label, color = colors.textPrimary) },
                        onClick = {
                            expanded = false
                            onSelect(index)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LoudnessSection(progress: Int, onProgressChange: (Int) -> Unit) {
    val colors = MaterialTheme.microBlastColors
    var showTooltip by remember { mutableStateOf(false) }
    val gain = 1.0f + (progress / 100f)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.loudness_label),
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
        )
        Box {
            IconButton(onClick = { showTooltip = true }, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = stringResource(R.string.loudness_info_cd),
                    tint = colors.accentCyan,
                    modifier = Modifier.size(16.dp),
                )
            }
            if (showTooltip) {
                LaunchedEffect(Unit) {
                    delay(4000)
                    showTooltip = false
                }
                Popup(
                    alignment = Alignment.TopStart,
                    offset = IntOffset(0, -140),
                    properties = PopupProperties(focusable = false),
                    onDismissRequest = { showTooltip = false },
                ) {
                    Box(
                        modifier = Modifier
                            .width(230.dp)
                            .background(colors.surfaceAlt, RoundedCornerShape(10.dp))
                            .border(1.dp, colors.borderFaint, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                    ) {
                        Text(text = stringResource(R.string.loudness_tooltip), color = colors.textPrimary, fontSize = 12.sp)
                    }
                }
            }
        }
        Box(modifier = Modifier.weight(1f))
        Text(text = "%.1f×".format(gain), color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }

    NeonSlider(progress = progress, onProgressChange = onProgressChange, enabled = true)

    Row(modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.loudness_min), color = colors.textSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
        Text(
            stringResource(R.string.loudness_max),
            color = colors.textSecondary,
            fontSize = 11.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ModeGrid(currentMode: String, onModeSelect: (String) -> Unit) {
    val colors = MaterialTheme.microBlastColors
    val modes = listOf(
        VoiceModeUi(AudioLoopbackService.MODE_NORMAL, stringResource(R.string.mode_normal), colors.accentCyan),
        VoiceModeUi(AudioLoopbackService.MODE_CHIPMUNK, stringResource(R.string.mode_chipmunk), colors.accentMagenta),
        VoiceModeUi(AudioLoopbackService.MODE_DEEP, stringResource(R.string.mode_deep), colors.accentGreen),
        VoiceModeUi(AudioLoopbackService.MODE_ROBOT, stringResource(R.string.mode_robot), colors.accentPurple),
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surfaceAlt),
        border = BorderStroke(1.dp, colors.borderFaint),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            modes.chunked(2).forEach { rowModes ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowModes.forEach { mode ->
                        ModeButton(
                            mode = mode,
                            selected = mode.id == currentMode,
                            onClick = { onModeSelect(mode.id) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeButton(mode: VoiceModeUi, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.microBlastColors
    val borderColor = if (selected) mode.color else colors.borderFaint

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) mode.color.copy(alpha = 0.12f) else Color.Transparent)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = mode.label, color = mode.color, fontSize = 13.sp)
    }
}

@Composable
private fun IntensitySection(progress: Int, enabled: Boolean, onProgressChange: (Int) -> Unit) {
    val colors = MaterialTheme.microBlastColors
    val sectionAlpha = if (enabled) 1f else 0.4f

    Text(
        text = stringResource(R.string.intensity_label),
        color = colors.textPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        modifier = Modifier
            .padding(bottom = 6.dp)
            .alpha(sectionAlpha),
    )

    Box(modifier = Modifier.alpha(sectionAlpha)) {
        NeonSlider(progress = progress, onProgressChange = onProgressChange, enabled = enabled)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(sectionAlpha),
    ) {
        Text(stringResource(R.string.intensity_min), color = colors.textSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
        Text(
            stringResource(R.string.intensity_max),
            color = colors.textSecondary,
            fontSize = 11.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * A slider whose track and thumb blend from cyan to magenta along its
 * length — the Compose equivalent of the old ColorUtils.blendARGB thumb
 * tinting in MainActivity's updateThumbTint().
 */
@Composable
private fun NeonSlider(progress: Int, onProgressChange: (Int) -> Unit, enabled: Boolean) {
    val colors = MaterialTheme.microBlastColors
    val fraction = (progress / 100f).coerceIn(0f, 1f)
    val thumbColor = lerp(colors.accentCyan, colors.accentMagenta, fraction)

    Slider(
        value = progress.toFloat(),
        onValueChange = { onProgressChange(it.roundToInt()) },
        valueRange = 0f..100f,
        enabled = enabled,
        colors = SliderDefaults.colors(
            thumbColor = thumbColor,
            activeTrackColor = thumbColor,
            inactiveTrackColor = colors.surfaceAlt,
            disabledThumbColor = thumbColor,
            disabledActiveTrackColor = thumbColor,
            disabledInactiveTrackColor = colors.surfaceAlt,
        ),
    )
}

@Composable
private fun OrientationLockFab(locked: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.microBlastColors
    IconButton(
        onClick = onToggle,
        modifier = modifier
            .size(36.dp)
            .border(1.dp, colors.borderFaint, CircleShape),
    ) {
        Icon(
            imageVector = if (locked) Icons.Filled.ScreenLockPortrait else Icons.Filled.RestartAlt,
            contentDescription = null,
            tint = colors.accentCyan,
            modifier = Modifier.size(18.dp),
        )
    }
}

/**
 * Full-screen slide-to-unlock. Matches the old behavior of forwarding a
 * touch from anywhere on the overlay onto the lock bar: the drag gesture is
 * attached to the whole Box, not just the thin track, and only commits
 * (calls onUnlock) past 95% of the track width — otherwise it springs back.
 */
@Composable
private fun LockOverlay(onUnlock: () -> Unit) {
    val colors = MaterialTheme.microBlastColors
    val density = LocalDensity.current
    val thumbSizePx = with(density) { 44.dp.toPx() }
    var trackWidthPx by remember { mutableFloatStateOf(1f) }
    var dragPx by remember { mutableFloatStateOf(0f) }
    val maxDragPx = (trackWidthPx - thumbSizePx).coerceAtLeast(1f)
    val fraction = (dragPx / maxDragPx).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { dragPx = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragPx = (dragPx + dragAmount).coerceIn(0f, maxDragPx)
                    },
                    onDragEnd = {
                        if (fraction >= 0.95f) onUnlock()
                        dragPx = 0f
                    },
                    onDragCancel = { dragPx = 0f },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.LockOpen, contentDescription = null, tint = colors.accentMagenta, modifier = Modifier.size(40.dp))
            VSpace(16.dp)
            Text(text = stringResource(R.string.slide_to_unlock), color = colors.textPrimary, fontSize = 14.sp)
            VSpace(28.dp)
            Box(
                modifier = Modifier
                    .width(260.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(colors.surfaceAlt)
                    .border(1.dp, colors.borderFaint, RoundedCornerShape(26.dp))
                    .onGloballyPositioned { coordinates -> trackWidthPx = coordinates.size.width.toFloat() }
                    .padding(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(colors.accentCyan, colors.accentMagenta),
                                endX = dragPx + thumbSizePx,
                            ),
                        ),
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .offset { IntOffset(dragPx.roundToInt(), 0) }
                        .clip(CircleShape)
                        .background(lerp(colors.accentCyan, colors.accentMagenta, fraction)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = colors.bgTop)
                }
            }
        }
    }
}
