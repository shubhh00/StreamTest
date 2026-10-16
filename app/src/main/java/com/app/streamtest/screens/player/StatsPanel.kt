package com.app.streamtest.screens.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.app.streamtest.player.LiveStats
import com.app.streamtest.ui.components.QualityLine
import com.app.streamtest.ui.theme.DimTextColor
import com.app.streamtest.ui.theme.ProblemColor
import com.app.streamtest.ui.theme.SecondaryTextColor
import com.app.streamtest.ui.theme.StatNumberStyle
import com.app.streamtest.ui.theme.tabularDigits
import com.app.streamtest.ui.theme.TextColor

/** Bottom panel: title + time, the six big numbers, and the quality line. */
@Composable
fun StatsPanel(title: String, stats: LiveStats, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.weight(1f))
            Text(
                text = tabularDigits(timeText(stats)),
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryTextColor,
            )
            Spacer(Modifier.width(24.dp))
            Text(text = "▲ hide stats   ▼ quality", style = MaterialTheme.typography.bodySmall, color = DimTextColor)
        }
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            StatItem(
                value = formatPlayingAt(stats.videoWidth, stats.videoHeight, stats.codecs, stats.sampleMimeType),
                label = "playing at",
                weight = 1.5f,
            )
            // Widths roughly follow how long each value can get.
            StatItem(value = formatBitrate(stats.bitrate), label = "bitrate", weight = 1.25f)
            StatItem(value = formatSeconds(stats.bufferedAheadMs), label = "buffered ahead", weight = 1.1f)
            StatItem(value = formatMs(stats.milestones.firstFrameMs), label = "time to start", weight = 1.2f)
            StatItem(
                value = stats.freezeCount.toString(),
                label = if (stats.freezeCount > 0) "freezes · ${formatSeconds(stats.freezeTotalMs)}" else "freezes",
                weight = 0.9f,
                // Color only appears on problems.
                color = if (stats.freezeCount > 0) ProblemColor else TextColor,
            )
            StatItem(value = stats.droppedFrames.toString(), label = "dropped frames", weight = 0.9f)
        }
        Spacer(Modifier.height(10.dp))
        QualityLine(model = stats.qualityLine, playheadFraction = stats.playheadFraction)
    }
}

@Composable
private fun RowScope.StatItem(value: String, label: String, weight: Float = 1f, color: Color = TextColor) {
    Column(modifier = Modifier.weight(weight)) {
        Text(text = tabularDigits(value), style = StatNumberStyle, color = color, maxLines = 1)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = SecondaryTextColor, maxLines = 1)
    }
}

private fun timeText(stats: LiveStats): String {
    val duration = stats.durationMs
    return when {
        stats.isLive -> "LIVE · watched ${formatClock(stats.positionMs)}"
        duration != null -> "${formatClock(stats.positionMs)} / ${formatClock(duration)}"
        else -> formatClock(stats.positionMs)
    }
}
