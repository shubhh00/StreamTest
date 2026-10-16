package com.app.streamtest.screens.player

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.app.streamtest.player.LiveStats
import com.app.streamtest.player.QualityOption
import com.app.streamtest.ui.components.StreamRow
import com.app.streamtest.ui.theme.DimTextColor
import com.app.streamtest.ui.theme.SecondaryTextColor

/**
 * Top-left panel: Auto (with what Auto is playing right now), then every quality the
 * stream offers. OK on a row forces it; OK on Auto lets the player choose again.
 */
@Composable
fun QualityPanel(
    qualities: List<QualityOption>,
    forcedQualityKey: String?,
    stats: LiveStats,
    /** Attached to the currently chosen row, so focus starts there when the panel opens. */
    initialFocus: FocusRequester,
    onQualitySelected: (key: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            // focusGroup + onExit = cancel: while the panel is open, D-pad focus can't
            // leave it (pressing past the first/last row does nothing).
            .focusProperties { onExit = { cancelFocusChange() } }
            .focusGroup(),
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(text = "Quality", style = MaterialTheme.typography.labelMedium, color = SecondaryTextColor)
            Spacer(Modifier.weight(1f))
            Text(text = "BACK closes", style = MaterialTheme.typography.labelSmall, color = DimTextColor)
        }
        // "Auto shows its choice": what the player picked by itself, right now.
        val width = stats.videoWidth
        val height = stats.videoHeight
        val autoNow = if (width != null && height != null) formatResolutionName(width, height) else "—"
        StreamRow(
            title = "Auto",
            subtitle = "now $autoNow · ${formatBitrate(stats.bitrate)}",
            result = if (forcedQualityKey == null) "selected" else "",
            onClick = { onQualitySelected(null) },
            modifier = if (forcedQualityKey == null) Modifier.focusRequester(initialFocus) else Modifier,
        )
        qualities.forEach { quality ->
            val isForced = quality.key == forcedQualityKey
            StreamRow(
                title = formatResolutionName(quality.width, quality.height),
                subtitle = "${quality.width}×${quality.height} · ${formatBitrate(quality.bitrate)}",
                result = if (isForced) "selected" else "",
                onClick = { onQualitySelected(quality.key) },
                modifier = if (isForced) Modifier.focusRequester(initialFocus) else Modifier,
            )
        }
        if (qualities.isEmpty()) {
            Text(
                text = "Waiting for the stream's qualities…",
                style = MaterialTheme.typography.bodySmall,
                color = DimTextColor,
                modifier = Modifier.fillMaxWidth().padding(12.dp),
            )
        }
    }
}
