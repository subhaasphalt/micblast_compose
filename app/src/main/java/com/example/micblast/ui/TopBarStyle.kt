package com.example.micblast.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.micblast.ui.theme.microBlastColors

/**
 * Shape + padding shared by the top "island" bar on MainScreen (menu/title/
 * lock) and SettingsScreen (back + quick toggles). Previously each screen
 * hand-rolled its own values (Main had no card at all; Settings used its
 * own local RoundedCornerShape(20.dp)), so navigating between the two
 * showed a visible jump in the top chrome's shape and inset. Both screens
 * now pull from here instead.
 */
// Percent-based so the corners are always fully rounded into a true pill
// regardless of the row's actual height (40dp buttons + 10dp vertical
// padding today, but this way it stays a pill even if those change) —
// the straight edges of a fixed-radius rounded rect didn't line up with
// the circular buttons sitting inside it.
val TopBarIslandShape = RoundedCornerShape(percent = 50)
val TopBarIslandPadding: Dp = 10.dp

/**
 * One circular nav/action button for the top island — menu and lock on
 * MainScreen, back on SettingsScreen. All three are conceptually the same
 * kind of control (a single icon-only tap target living in the top island),
 * so they now share one size, one fill, and one double-ring border
 * treatment (faint neutral ring, then a tinted accent ring on top — keeps
 * the edge readable even when [tint] sits close in value to the
 * background). Only [tint] changes per button to signal what it does.
 */
@Composable
fun TopBarIconButton(
    icon: ImageVector,
    contentDescription: String?,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val colors = MaterialTheme.microBlastColors
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .background(colors.surfaceChip, CircleShape)
            .border(1.dp, colors.borderFaint, CircleShape)
            .padding(1.dp)
            .border(1.5.dp, tint.copy(alpha = 0.78f), CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}
