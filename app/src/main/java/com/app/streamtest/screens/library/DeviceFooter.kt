package com.app.streamtest.screens.library

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.app.streamtest.domain.device.DeviceSummary
import com.app.streamtest.ui.theme.DimTextColor
import com.app.streamtest.ui.theme.ProblemColor
import com.app.streamtest.ui.theme.SecondaryTextColor

/** "Plays: …" in grey, "Can't play: …" in the problem color, remote hints on the right. */
@Composable
fun DeviceFooter(summary: DeviceSummary?, modifier: Modifier = Modifier) {
    val style = MaterialTheme.typography.bodySmall
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (summary == null) {
            Text(text = "Checking this TV…", style = style, color = SecondaryTextColor)
        } else {
            if (summary.canPlay.isNotEmpty()) {
                Text(text = "Plays: " + summary.canPlay.joinToString(" · "), style = style, color = SecondaryTextColor)
                Spacer(Modifier.width(24.dp))
            }
            if (summary.cantPlay.isNotEmpty()) {
                Text(text = "Can't play: " + summary.cantPlay.joinToString(" · "), style = style, color = ProblemColor)
            }
        }
        Spacer(Modifier.weight(1f))
        Text(text = "▲▼ choose   ▶ versions   OK play", style = style, color = DimTextColor)
    }
}
