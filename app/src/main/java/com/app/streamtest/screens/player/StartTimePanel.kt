package com.app.streamtest.screens.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.app.streamtest.domain.stats.StartMilestones
import com.app.streamtest.domain.stats.StartStepKind
import com.app.streamtest.domain.stats.steps
import com.app.streamtest.ui.theme.DimTextColor
import com.app.streamtest.ui.theme.SecondaryTextColor
import com.app.streamtest.ui.theme.StatNumberStyle
import com.app.streamtest.ui.theme.tabularDigits

/**
 * Top-right panel: total time to the first frame, then each step and the time it
 * added since the previous step. Some steps overlap (e.g. the DRM license can arrive
 * while the first chunk downloads), so these are milestones, not exact costs.
 */
@Composable
fun StartTimePanel(milestones: StartMilestones, isProtected: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = "Start time, step by step", style = MaterialTheme.typography.labelMedium, color = SecondaryTextColor)
        Text(text = tabularDigits(formatMs(milestones.firstFrameMs)), style = StatNumberStyle)
        Spacer(Modifier.height(8.dp))
        milestones.steps().forEach { step ->
            val value = when {
                step.gapMs != null -> "+${step.gapMs} ms"
                // A clear stream never has a DRM step.
                step.kind == StartStepKind.DRM_LICENSE && !isProtected -> "—"
                else -> "…"
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = step.kind.label(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (step.gapMs == null) DimTextColor else SecondaryTextColor,
                    modifier = Modifier.weight(1f),
                )
                Text(text = tabularDigits(value), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
