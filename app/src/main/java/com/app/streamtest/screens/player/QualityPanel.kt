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
        val rowTexts = qualityRowTexts(qualities)
        qualities.forEachIndexed { index, quality ->
            val isForced = quality.key == forcedQualityKey
            StreamRow(
                title = rowTexts[index].title,
                subtitle = rowTexts[index].subtitle,
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

/** What one quality row says. */
internal data class QualityRowText(val title: String, val subtitle: String)

/**
 * Usually "1080p" over "1920×856 · 4.2 Mbps". Streams often offer the same size at two
 * bitrates (e.g. 1680×750 at 1.7 and 2.5 Mbps); two rows both called "1080p" would look
 * like a mistake, so then the bitrate moves into the title: "1080p · 2.5 Mbps".
 */
internal fun qualityRowTexts(qualities: List<QualityOption>): List<QualityRowText> {
    val names = qualities.map { formatResolutionName(it.width, it.height) }
    return qualities.mapIndexed { index, quality ->
        val name = names[index]
        val size = "${quality.width}×${quality.height}"
        val bitrate = formatBitrate(quality.bitrate)
        if (names.count { it == name } > 1) {
            QualityRowText(title = "$name · $bitrate", subtitle = size)
        } else {
            QualityRowText(title = name, subtitle = "$size · $bitrate")
        }
    }
}
