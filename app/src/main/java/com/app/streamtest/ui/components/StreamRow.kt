package com.app.streamtest.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.app.streamtest.domain.stats.QualityLineModel
import com.app.streamtest.ui.theme.DimTextColor
import com.app.streamtest.ui.theme.ProblemColor
import com.app.streamtest.ui.theme.tabularDigits

/**
 * One playable row: name and a one-line subtitle on the left, the last result on
 * the right, and (once tested) that test's quality line under the result.
 * Used for film versions, "More streams", the Quality panel and Settings.
 */
@Composable
fun StreamRow(
    title: String,
    subtitle: String,
    result: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onFocused: () -> Unit = {},
    qualityLine: QualityLineModel? = null,
    /** Show the result in the problem color (e.g. the last test failed). */
    resultIsProblem: Boolean = false,
) {
    FocusFillSurface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { if (it.isFocused) onFocused() },
    ) { focused ->
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryTextColor(focused),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = tabularDigits(result),
                    style = MaterialTheme.typography.bodySmall,
                    // Color only appears on problems; on the off-white focus fill keep it readable.
                    color = if (resultIsProblem && !focused) ProblemColor else secondaryTextColor(focused),
                )
                if (qualityLine != null) {
                    QualityLine(
                        model = qualityLine,
                        thickness = 2.dp,
                        // On the off-white focus fill the line switches to dark shades.
                        onLightBackground = focused,
                        modifier = Modifier.padding(top = 6.dp).width(104.dp),
                    )
                }
            }
        }
    }
}

/** A version that doesn't exist for this film: shown, but dim and not focusable. */
@Composable
fun UnavailableStreamRow(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = DimTextColor)
            Text(text = "Not available", style = MaterialTheme.typography.bodySmall, color = DimTextColor)
        }
    }
}
