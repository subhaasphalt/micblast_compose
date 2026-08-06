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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
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

            ModeAndIntensityGroup(
                currentMode = currentMode,
                onModeSelect = onModeSelect,
                intensityProgress = intensityProgress,
                intensityEnabled = currentMode != AudioLoopbackService.MODE_NORMAL,
                onIntensityChange = onIntensityChange,
            )
        }

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
        Box(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        tint = colors.textSecondary,
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedTextColor = colors.textSecondary,
                    unfocusedTextColor = colors.textSecondary,
                    focusedIndicatorColor = colors.borderFaint,
                    unfocusedIndicatorColor = colors.borderFaint,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            // Transparent overlay so taps toggle the menu even though the field itself is read-only.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = !expanded },
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(),
            ) {
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

/**
 * Voice modes and their intensity live in one card so both controls sit
 * under the same thumb: the 2x2 mode grid on the left, a compact vertical
 * intensity slider on the right — reachable without shifting grip when
 * holding the phone one-handed.
 */
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
        Row(
            modifier = Modifier
                .padding(14.dp)
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
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

            IntensityColumn(
                progress = intensityProgress,
                enabled = intensityEnabled,
                onProgressChange = onIntensityChange,
                modifier = Modifier.fillMaxHeight(),
            )
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

/**
 * Compact vertical companion to the mode grid: "Extreme" sits at the top,
 * "Subtle" at the bottom, and the slider is thumb-height rather than
 * full-width, matching the small footprint this control gets next to the
 * grid.
 */
@Composable
private fun IntensityColumn(
    progress: Int,
    enabled: Boolean,
    onProgressChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.microBlastColors
    val sectionAlpha = if (enabled) 1f else 0.4f

    Column(
        modifier = modifier
            .width(48.dp)
            .alpha(sectionAlpha),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.intensity_label),
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Text(stringResource(R.string.intensity_max), color = colors.textSecondary, fontSize = 10.sp)
        VerticalNeonSlider(
            progress = progress,
            onProgressChange = onProgressChange,
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 2.dp),
        )
        Text(stringResource(R.string.intensity_min), color = colors.textSecondary, fontSize = 10.sp)
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

/**
 * Compose has no built-in vertical slider, so this rotates a normal one
 * 270° and swaps its measured width/height — the standard workaround.
 * Dragging up increases the value, matching the "Extreme" label above it.
 */
@Composable
private fun VerticalNeonSlider(
    progress: Int,
    onProgressChange: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
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
        modifier = modifier
            .graphicsLayer {
                rotationZ = 270f
                transformOrigin = TransformOrigin(0f, 0f)
            }
            .layout { measurable, constraints ->
                val placeable = measurable.measure(
                    Constraints(
                        minWidth = constraints.minHeight,
                        maxWidth = constraints.maxHeight,
                        minHeight = constraints.minWidth,
                        maxHeight = constraints.maxWidth,
                    ),
                )
                layout(placeable.height, placeable.width) {
                    placeable.place(-placeable.width + placeable.height, 0)
                }
            },
    )
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
                // pointerInput(Unit) builds this whole gesture-callback
                // block exactly once and never rebuilds it on recomposition
                // (its key never changes) — so any *plain* local value
                // captured here (like the outer `maxDragPx`/`fraction` vals)
                // stays frozen forever at whatever it was during that first
                // build, which happens before layout has measured the
                // track (trackWidthPx still at its 1f default). That
                // freeze is the actual cause of the old bug: the drag still
                // *rendered* correctly (the Box below reads live state
                // directly), but the release check compared against a
                // bound computed from an unmeasured track, so it could
                // never legitimately reach 0.95 and onUnlock() never fired.
                // trackWidthPx/dragPx themselves are `remember`ed state, so
                // reading THEM (not a derived val) inside this closure is
                // always live — recomputing the bound from them inline,
                // every time, is what actually fixes it.
                detectHorizontalDragGestures(
                    onDragStart = { dragPx = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        val liveMaxDragPx = (trackWidthPx - thumbSizePx).coerceAtLeast(1f)
                        dragPx = (dragPx + dragAmount).coerceIn(0f, liveMaxDragPx)
                    },
                    onDragEnd = {
                        val liveMaxDragPx = (trackWidthPx - thumbSizePx).coerceAtLeast(1f)
                        val releaseFraction = (dragPx / liveMaxDragPx).coerceIn(0f, 1f)
                        if (releaseFraction >= 0.95f) onUnlock()
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
