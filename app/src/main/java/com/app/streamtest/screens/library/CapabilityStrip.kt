package com.app.streamtest.screens.library

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.app.streamtest.domain.device.DeviceSummary
import com.app.streamtest.ui.theme.LineColor
import com.app.streamtest.ui.theme.ProblemColor
import com.app.streamtest.ui.theme.SecondaryTextColor
import com.app.streamtest.ui.theme.TextColor

/**
 * "What this TV can play": one outlined chip per decoder / DRM capability, then the
 * screen on its own (decoding 4K and showing 4K are different), what's actually been
 * played in tests, and what it can't play.
 */
@Composable
fun CapabilityStrip(summary: DeviceSummary?, playedUpToHeight: Int?, modifier: Modifier = Modifier) {
    val style = MaterialTheme.typography.labelMedium
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // Sits beside the chips, so it lines up with them (they have 3dp of padding).
        Text(text = "This TV", style = style, color = SecondaryTextColor, modifier = Modifier.padding(top = 3.dp))
        if (summary == null) {
            Text(text = "checking…", style = style, color = SecondaryTextColor, modifier = Modifier.padding(top = 3.dp))
            return@Row
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            DecoderLine(summary, style)
            // Second line: the screen on its own, and what tests actually played.
            val played = if (playedUpToHeight != null) " · played up to ${playedUpToHeight}p" else ""
            Text(text = summary.screen + played, style = style, color = SecondaryTextColor)
        }
    }
}

/** The capability chips, then what it can't play. */
@Composable
private fun DecoderLine(summary: DeviceSummary, style: TextStyle) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        summary.canPlay.forEach { capability ->
            Text(
                text = capability,
                style = style,
                color = TextColor,
                modifier = Modifier
                    .border(width = 1.dp, color = LineColor, shape = RoundedCornerShape(2.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        if (summary.cantPlay.isNotEmpty()) {
            Spacer(Modifier.width(8.dp))
            // Color only appears on problems.
            Text(text = "Can't play: " + summary.cantPlay.joinToString(" · "), style = style, color = ProblemColor)
        }
    }
}
