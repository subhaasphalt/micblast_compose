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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
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
    val color: Color,
    val icon: ImageVector
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B0F1A),
                        Color(0xFF12101F),
                        Color(0xFF0D0B14)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            TopBar(onMenuClick = onMenuClick, onLockClick = onLockClick)

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                PrimaryActionButton(
                    isRunning = isRunning,
                    onPlayClick = onPlayClick,
                    onStopClick = onStopClick
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            AudioSetupDropdown(
                selectedIndex = audioSetupIndex,
                labels = audioSetupLabels,
                onSelect = onAudioSetupSelect
            )

            Spacer(modifier = Modifier.height(24.dp))

            LoudnessSection(
                progress = gainProgress,
                onProgressChange = onGainChange
            )

            Spacer(modifier = Modifier.height(24.dp))

            ModeAndIntensityGroup(
                currentMode = currentMode,
                onModeSelect = onModeSelect,
                intensityProgress = intensityProgress,
                intensityEnabled = currentMode != AudioLoopbackService.MODE_NORMAL,
                onIntensityChange = onIntensityChange
            )
        }

        if (isLocked) {
            LockOverlay(onUnlock = onUnlock)
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
                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
        ) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = stringResource(R.string.menu_button_cd),
                tint = Color(0xFF00E5FF)
            )
        }

        Text(
            text = stringResource(R.string.app_title),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onLockClick,
            modifier = Modifier
                .size(44.dp)
                .border(1.5.dp, Color(0xFFFF2D95), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = stringResource(R.string.lock_button_cd),
                tint = Color(0xFFFF2D95)
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
    AnimatedContent(
        targetState = isRunning,
        transitionSpec = {
            scaleIn(tween(260), initialScale = 0.7f) togetherWith
                    scaleOut(tween(200), targetScale = 0.7f)
        },
        contentAlignment = Alignment.Center,
        label = "playStopMorph"
    ) { running ->
        val diameter = if (running) 86.dp else 96.dp
        val accent = if (running) Color(0xFFFF2D95) else Color(0xFF00E5FF)

        Box(
            modifier = Modifier
                .size(diameter)
                .clip(CircleShape)
                .background(Color(0xFF16131F))
                .border(2.5.dp, accent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = if (running) onStopClick else onPlayClick) {
                Icon(
                    imageVector = if (running) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(38.dp)
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
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = labels.getOrElse(selectedIndex) { labels.firstOrNull().orEmpty() }

    Column {
        Text(
            text = stringResource(R.string.audio_setup_label),
            color = Color.White,
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
                        tint = Color(0xFFAAAAAA)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1A1625),
                    unfocusedContainerColor = Color(0xFF1A1625),
                    focusedTextColor = Color(0xFFDDDDDD),
                    unfocusedTextColor = Color(0xFFDDDDDD),
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
                        text = { Text(label, color = Color.White) },
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
    var showTooltip by remember { mutableStateOf(false) }
    val gain = 1.0f + (progress / 100f)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.loudness_label),
            color = Color.White,
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
                    tint = Color(0xFF00E5FF),
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
                            .background(Color(0xFF1E1A2A), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF33304A), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.loudness_tooltip),
                            color = Color(0xFFEEEEEE),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "%.1f×".format(gain),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }

    Spacer(modifier = Modifier.height(6.dp))

    NeonSlider(
        progress = progress,
        onProgressChange = onProgressChange,
        enabled = true
    )

    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.loudness_min),
            color = Color(0xFF888888),
            fontSize = 11.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(R.string.loudness_max),
            color = Color(0xFF888888),
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
    val modes = listOf(
        VoiceModeUi(AudioLoopbackService.MODE_NORMAL, "Normal", Color(0xFF00E5FF), Icons.Filled.Mic),
        VoiceModeUi(AudioLoopbackService.MODE_CHIPMUNK, "Chipmunk", Color(0xFFFF2D95), Icons.Filled.Mic),
        VoiceModeUi(AudioLoopbackService.MODE_DEEP, "Monster", Color(0xFF39FF14), Icons.Filled.Mic),
        VoiceModeUi(AudioLoopbackService.MODE_ROBOT, "Robot", Color(0xFFBF5AF2), Icons.Filled.Mic)
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16131F)),
        border = BorderStroke(1.dp, Color(0xFF2A2438)),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
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
    val background = if (selected) mode.color.copy(alpha = 0.15f) else Color(0xFF1C1826)
    val borderColor = if (selected) mode.color else Color(0xFF2E2A3A)

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
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = mode.icon,
                contentDescription = null,
                tint = mode.color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
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
    val alpha = if (enabled) 1f else 0.38f

    Column(
        modifier = modifier
            .width(62.dp)
            .alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Intensity",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Extreme",
            color = Color(0xFFAAAAAA),
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
            text = "Subtle",
            color = Color(0xFFAAAAAA),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun NeonSlider(
    progress: Int,
    onProgressChange: (Int) -> Unit,
    enabled: Boolean
) {
    val fraction = (progress / 100f).coerceIn(0f, 1f)
    val thumbColor = lerp(Color(0xFF00E5FF), Color(0xFFFF2D95), fraction)

    Slider(
        value = progress.toFloat(),
        onValueChange = { onProgressChange(it.roundToInt()) },
        valueRange = 0f..100f,
        enabled = enabled,
        colors = SliderDefaults.colors(
            thumbColor = thumbColor,
            activeTrackColor = thumbColor,
            inactiveTrackColor = Color(0xFF2A2438),
            disabledThumbColor = thumbColor,
            disabledActiveTrackColor = thumbColor,
            disabledInactiveTrackColor = Color(0xFF2A2438)
        )
    )
}

@Composable
private fun VerticalNeonSlider(
    progress: Int,
    onProgressChange: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val fraction = (progress / 100f).coerceIn(0f, 1f)
    val thumbColor = lerp(Color(0xFF00E5FF), Color(0xFFFF2D95), fraction)
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
                color = Color(0xFF2A2438),
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
                color = Color(0xFF0B0F1A),
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
