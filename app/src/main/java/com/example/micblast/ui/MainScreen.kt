package com.example.micblast.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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

private data class VoiceModeUi(
    val id: String,
    val label: String,
    val color: Color
)

@Composable
fun MainScreen(
    isRunning: Boolean,
    currentMode: String,
    gainProgress: Int,
    intensityProgress: Int,
    audioSetupIndex: Int,
    audioSetupLabels: List<String>,
    isLocked: Boolean,
    hapticsEnabled: Boolean,
    onPlayClick: () -> Unit,
    onStopClick: () -> Unit,
    onModeSelect: (String) -> Unit,
    onGainChange: (Int) -> Unit,
    onIntensityChange: (Int) -> Unit,
    onAudioSetupSelect: (Int) -> Unit,
    onLockClick: () -> Unit,
    onUnlock: () -> Unit,
    onMenuClick: () -> Unit,
) {
    val colors = MaterialTheme.microBlastColors
    val hapticClick = rememberHapticClick(hapticsEnabled)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colors.bgTop,
                        colors.bgMid,
                        colors.bgBottom
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            TopBar(
                onMenuClick = { hapticClick(); onMenuClick() },
                onLockClick = { hapticClick(); onLockClick() },
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                PrimaryActionButton(
                    isRunning = isRunning,
                    onPlayClick = { hapticClick(); onPlayClick() },
                    onStopClick = { hapticClick(); onStopClick() }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            AudioSetupDropdown(
                selectedIndex = audioSetupIndex,
                labels = audioSetupLabels,
                onSelect = { index -> hapticClick(); onAudioSetupSelect(index) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            LoudnessSection(
                progress = gainProgress,
                onProgressChange = onGainChange
            )

            Spacer(modifier = Modifier.height(14.dp))

            ModeAndIntensityGroup(
                currentMode = currentMode,
                onModeSelect = { mode -> hapticClick(); onModeSelect(mode) },
                intensityProgress = intensityProgress,
                intensityEnabled = currentMode != AudioLoopbackService.MODE_NORMAL,
                onIntensityChange = onIntensityChange
            )
        }

        if (isLocked) {
            LockOverlay(onUnlock = { hapticClick(); onUnlock() })
        }
    }
}

@Composable
private fun TopBar(onMenuClick: () -> Unit, onLockClick: () -> Unit) {
    val colors = MaterialTheme.microBlastColors

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .size(42.dp)
                .border(1.dp, colors.accentCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
        ) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = stringResource(R.string.menu_button_cd),
                tint = colors.accentCyan
            )
        }

        Text(
            text = stringResource(R.string.app_title),
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onLockClick,
            modifier = Modifier
                .size(44.dp)
                .border(1.5.dp, colors.accentMagenta, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = stringResource(R.string.lock_button_cd),
                tint = colors.accentMagenta
            )
        }
    }
}

@Composable
private fun PrimaryActionButton(
    isRunning: Boolean,
    onPlayClick: () -> Unit,
    onStopClick: () -> Unit
) {
    val colors = MaterialTheme.microBlastColors

    val accent by androidx.compose.animation.animateColorAsState(
        targetValue = if (isRunning) {
            colors.accentMagenta
        } else {
            colors.accentCyan
        },
        animationSpec = tween(220),
        label = "primaryButtonAccent"
    )

    val iconScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isRunning) 1f else 0.92f,
        animationSpec = tween(180),
        label = "primaryButtonIconScale"
    )

    val iconRotation by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isRunning) 0f else -90f,
        animationSpec = tween(220),
        label = "primaryButtonIconRotation"
    )

    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(colors.surface)
            .border(
                width = 2.5.dp,
                color = accent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = if (isRunning) onStopClick else onPlayClick,
            modifier = Modifier.size(72.dp)
        ) {
            AnimatedContent(
                targetState = isRunning,
                transitionSpec = {
                    scaleIn(
                        animationSpec = tween(180),
                        initialScale = 0.65f
                    ) togetherWith scaleOut(
                        animationSpec = tween(120),
                        targetScale = 0.65f
                    )
                },
                contentAlignment = Alignment.Center,
                label = "playStopIcon"
            ) { running ->
                Icon(
                    imageVector = if (running) {
                        Icons.Filled.Stop
                    } else {
                        Icons.Filled.PlayArrow
                    },
                    contentDescription = if (running) {
                        "Stop"
                    } else {
                        "Play"
                    },
                    tint = accent,
                    modifier = Modifier
                        .size(38.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                            rotationZ = iconRotation
                        }
                )
            }
        }
    }
}

@Composable
private fun AudioSetupDropdown(
    selectedIndex: Int,
    labels: List<String>,
    onSelect: (Int) -> Unit
) {
    val colors = MaterialTheme.microBlastColors
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = labels.getOrElse(selectedIndex) { labels.firstOrNull().orEmpty() }

    Column {
        Text(
            text = stringResource(R.string.audio_setup_label),
            color = colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        tint = colors.textSecondary
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = !expanded }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                labels.forEachIndexed { index, label ->
                    DropdownMenuItem(
                        text = { Text(label, color = colors.textPrimary) },
                        onClick = {
                            expanded = false
                            onSelect(index)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoudnessSection(
    progress: Int,
    onProgressChange: (Int) -> Unit
) {
    val colors = MaterialTheme.microBlastColors
    var showTooltip by remember { mutableStateOf(false) }
    val gain = 1.0f + (progress / 100f)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.loudness_label),
            color = colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Box {
            IconButton(
                onClick = { showTooltip = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = stringResource(R.string.loudness_info_cd),
                    tint = colors.accentCyan,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (showTooltip) {
                LaunchedEffect(Unit) {
                    delay(3500)
                    showTooltip = false
                }
                Popup(
                    alignment = Alignment.TopStart,
                    offset = IntOffset(0, -130),
                    properties = PopupProperties(focusable = false),
                    onDismissRequest = { showTooltip = false }
                ) {
                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .background(colors.surface, RoundedCornerShape(12.dp))
                            .border(1.dp, colors.borderFaint, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.loudness_tooltip),
                            color = colors.textPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "%.1f×".format(gain),
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }

    Spacer(modifier = Modifier.height(6.dp))

    HorizontalNeonSlider(
        progress = progress,
        onProgressChange = onProgressChange,
        enabled = true
    )

    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.loudness_min),
            color = colors.textMuted,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(R.string.loudness_max),
            color = colors.textMuted,
            fontSize = 11.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ModeAndIntensityGroup(
    currentMode: String,
    onModeSelect: (String) -> Unit,
    intensityProgress: Int,
    intensityEnabled: Boolean,
    onIntensityChange: (Int) -> Unit,
) {
    val colors = MaterialTheme.microBlastColors
    val modes = listOf(
        VoiceModeUi(AudioLoopbackService.MODE_NORMAL, "Normal", colors.accentCyan),
        VoiceModeUi(AudioLoopbackService.MODE_CHIPMUNK, "Chipmunk", colors.accentMagenta),
        VoiceModeUi(AudioLoopbackService.MODE_DEEP, "Monster", colors.accentGreen),
        VoiceModeUi(AudioLoopbackService.MODE_ROBOT, "Robot", colors.accentPurple)
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.borderFaint),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                modes.chunked(2).forEach { rowModes ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowModes.forEach { mode ->
                            ModeButton(
                                mode = mode,
                                selected = mode.id == currentMode,
                                onClick = { onModeSelect(mode.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            IntensityColumn(
                progress = intensityProgress,
                enabled = intensityEnabled,
                onProgressChange = onIntensityChange,
                modifier = Modifier.fillMaxHeight()
            )
        }
    }
}

@Composable
private fun ModeButton(
    mode: VoiceModeUi,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.microBlastColors
    val background = if (selected) mode.color.copy(alpha = 0.15f) else colors.surfaceChip
    val borderColor = if (selected) mode.color else colors.borderFaint

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            VoiceModeIcon(
                modeId = mode.id,
                tint = mode.color,
                modifier = Modifier.size(34.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = mode.label,
                color = mode.color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun IntensityColumn(
    progress: Int,
    enabled: Boolean,
    onProgressChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.microBlastColors
    val alpha = if (enabled) 1f else 0.38f

    Column(
        modifier = modifier
            .width(62.dp)
            .alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Extreme",
            color = colors.textSecondary,
            fontSize = 10.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        VerticalNeonSlider(
            progress = progress,
            onProgressChange = onProgressChange,
            enabled = enabled,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Normal",
            color = colors.textSecondary,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun HorizontalNeonSlider(
    progress: Int,
    onProgressChange: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.microBlastColors
    val fraction = (progress / 100f).coerceIn(0f, 1f)
    val thumbColor = lerp(colors.accentCyan, colors.accentMagenta, fraction)
    val density = LocalDensity.current
    val thumbRadiusPx = with(density) { 9.dp.toPx() }
    val trackStroke = with(density) { 5.dp.toPx() }
    var widthPx by remember { mutableFloatStateOf(1f) }

    fun updateFromX(x: Float) {
        val usable = (widthPx - 2 * thumbRadiusPx).coerceAtLeast(1f)
        val clamped = (x - thumbRadiusPx).coerceIn(0f, usable)
        val newFraction = clamped / usable
        onProgressChange((newFraction * 100f).roundToInt().coerceIn(0, 100))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(30.dp)
            .onGloballyPositioned { widthPx = it.size.width.toFloat() }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures { offset -> updateFromX(offset.x) }
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    updateFromX(change.position.x)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val usableWidth = (size.width - 2 * thumbRadiusPx).coerceAtLeast(1f)
            val centerY = size.height / 2f
            val trackStart = thumbRadiusPx
            val trackEnd = size.width - thumbRadiusPx
            val thumbX = trackStart + usableWidth * fraction

            // inactive track
            drawLine(
                color = colors.borderFaint,
                start = Offset(trackStart, centerY),
                end = Offset(trackEnd, centerY),
                strokeWidth = trackStroke,
                cap = StrokeCap.Round
            )

            // active track
            if (fraction > 0f) {
                drawLine(
                    color = thumbColor,
                    start = Offset(trackStart, centerY),
                    end = Offset(thumbX, centerY),
                    strokeWidth = trackStroke,
                    cap = StrokeCap.Round
                )
            }

            // thumb
            drawCircle(
                color = thumbColor,
                radius = thumbRadiusPx,
                center = Offset(thumbX, centerY)
            )
            drawCircle(
                color = colors.bgTop,
                radius = thumbRadiusPx * 0.38f,
                center = Offset(thumbX, centerY)
            )
        }
    }
}

@Composable
private fun VerticalNeonSlider(
    progress: Int,
    onProgressChange: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.microBlastColors
    val fraction = (progress / 100f).coerceIn(0f, 1f)
    val thumbColor = lerp(colors.accentCyan, colors.accentMagenta, fraction)
    val density = LocalDensity.current
    val thumbRadiusPx = with(density) { 9.dp.toPx() }
    val trackStroke = with(density) { 5.dp.toPx() }
    var heightPx by remember { mutableFloatStateOf(1f) }

    fun updateFromY(y: Float) {
        val usable = (heightPx - 2 * thumbRadiusPx).coerceAtLeast(1f)
        val clamped = (y - thumbRadiusPx).coerceIn(0f, usable)
        val newFraction = 1f - (clamped / usable)
        onProgressChange((newFraction * 100f).roundToInt().coerceIn(0, 100))
    }

    Box(
        modifier = modifier
            .width(30.dp)
            .fillMaxHeight()
            .onGloballyPositioned { heightPx = it.size.height.toFloat() }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures { offset -> updateFromY(offset.y) }
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectVerticalDragGestures { change, _ ->
                    change.consume()
                    updateFromY(change.position.y)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val usableHeight = (size.height - 2 * thumbRadiusPx).coerceAtLeast(1f)
            val centerX = size.width / 2f
            val trackTop = thumbRadiusPx
            val trackBottom = size.height - thumbRadiusPx
            val thumbY = trackBottom - usableHeight * fraction

            // inactive track
            drawLine(
                color = colors.borderFaint,
                start = Offset(centerX, trackTop),
                end = Offset(centerX, trackBottom),
                strokeWidth = trackStroke,
                cap = StrokeCap.Round
            )

            // active track
            if (fraction > 0f) {
                drawLine(
                    color = thumbColor,
                    start = Offset(centerX, trackBottom),
                    end = Offset(centerX, thumbY),
                    strokeWidth = trackStroke,
                    cap = StrokeCap.Round
                )
            }

            // thumb
            drawCircle(
                color = thumbColor,
                radius = thumbRadiusPx,
                center = Offset(centerX, thumbY)
            )
            drawCircle(
                color = colors.surface,
                radius = thumbRadiusPx * 0.38f,
                center = Offset(centerX, thumbY)
            )
        }
    }
}

@Composable
private fun LockOverlay(onUnlock: () -> Unit) {
    val density = LocalDensity.current
    val thumbSizePx = with(density) { 44.dp.toPx() }
    var trackWidthPx by remember { mutableFloatStateOf(1f) }
    var dragPx by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { dragPx = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        val liveMax = (trackWidthPx - thumbSizePx).coerceAtLeast(1f)
                        dragPx = (dragPx + dragAmount).coerceIn(0f, liveMax)
                    },
                    onDragEnd = {
                        val liveMax = (trackWidthPx - thumbSizePx).coerceAtLeast(1f)
                        if ((dragPx / liveMax) >= 0.95f) onUnlock()
                        dragPx = 0f
                    },
                    onDragCancel = { dragPx = 0f }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.LockOpen,
                contentDescription = null,
                tint = Color(0xFFFF2D95),
                modifier = Modifier.size(42.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.slide_to_unlock),
                color = Color.White,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .width(260.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFF1A1625))
                    .border(1.dp, Color(0xFF33304A), RoundedCornerShape(26.dp))
                    .padding(4.dp)
                    .onGloballyPositioned { trackWidthPx = it.size.width.toFloat() }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF00E5FF), Color(0xFFFF2D95)),
                                endX = dragPx + thumbSizePx
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .offset { IntOffset(dragPx.roundToInt(), 0) }
                        .clip(CircleShape)
                        .background(
                            lerp(Color(0xFF00E5FF), Color(0xFFFF2D95), (dragPx / (trackWidthPx - thumbSizePx)).coerceIn(0f, 1f))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFF0B0F1A)
                    )
                }
            }
        }
    }
}

/**
 * Shown when the user presses the system back button from the main screen,
 * so a stray back press can't silently kill an active loopback session.
 */
@Composable
fun ExitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.microBlastColors

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        titleContentColor = colors.textPrimary,
        textContentColor = colors.textSecondary,
        title = { Text(stringResource(R.string.exit_confirm_title)) },
        text = { Text(stringResource(R.string.exit_confirm_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.exit_confirm_yes),
                    color = colors.accentMagenta,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.exit_confirm_cancel),
                    color = colors.accentCyan
                )
            }
        }
    )
}
