package com.example.micblast.ui

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.micblast.AudioLoopbackService
import com.example.micblast.R

/**
 * Maps a voice mode id to its icon resource. Icons live in res/drawable as
 * monochrome VectorDrawables (single opaque color, no fill/stroke color
 * baked in) so [tint] recolors them cleanly at render time — the artwork
 * itself never needs to know about the app's theme or accent colors.
 */
@DrawableRes
fun voiceModeIconRes(modeId: String): Int = when (modeId) {
    AudioLoopbackService.MODE_CHIPMUNK -> R.drawable.ic_voice_chipmunk
    AudioLoopbackService.MODE_DEEP -> R.drawable.ic_voice_monster
    AudioLoopbackService.MODE_ROBOT -> R.drawable.ic_voice_robot
    else -> R.drawable.ic_voice_reverb
}

@Composable
fun VoiceModeIcon(
    modeId: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(id = voiceModeIconRes(modeId)),
        contentDescription = null,
        tint = tint,
        modifier = modifier
    )
}
