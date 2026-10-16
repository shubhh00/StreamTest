package com.app.streamtest.screens.log

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.app.streamtest.ui.components.QualityLine
import com.app.streamtest.ui.label
import com.app.streamtest.ui.theme.FocusCornerRadius
import com.app.streamtest.ui.theme.ProblemColor
import com.app.streamtest.ui.theme.RowFocusColor
import com.app.streamtest.ui.theme.SecondaryTextColor
import com.app.streamtest.ui.theme.TabularNumbers
import com.app.streamtest.ui.theme.TextColor
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// Column widths, shared by the header and every row so they line up.
private val FilmWidth = 190.dp
private val VersionWidth = 100.dp
private val ToStartWidth = 90.dp
private val FreezesWidth = 70.dp
private val WhenWidth = 120.dp

@Composable
fun LogTableHeader() {
    val style = MaterialTheme.typography.labelSmall
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Cell("Film", FilmWidth, style, SecondaryTextColor)
        Cell("Version", VersionWidth, style, SecondaryTextColor)
        Cell("To start", ToStartWidth, style, SecondaryTextColor)
        Cell("Freezes", FreezesWidth, style, SecondaryTextColor)
        Text(text = "Quality over time", style = style, color = SecondaryTextColor, modifier = Modifier.weight(1f))
        Cell("When", WhenWidth, style, SecondaryTextColor, Modifier.padding(start = 16.dp))
    }
}

/**
 * One test. Rows are focusable so ▲▼ moves through them; the focused row gets a
 * subtle lighter background (not the off-white fill: there's nothing to press).
 */
@Composable
fun LogTableRow(row: LogRow, modifier: Modifier = Modifier) {
    val result = row.result
    val numberStyle = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = TabularNumbers)
    Surface(
        onClick = {}, // no actions in v1
        modifier = modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(FocusCornerRadius)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = RowFocusColor,
            pressedContainerColor = RowFocusColor,
            contentColor = TextColor,
            focusedContentColor = TextColor,
            pressedContentColor = TextColor,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Cell(if (row.showFilmTitle) result.filmTitle else "", FilmWidth, MaterialTheme.typography.bodyMedium, TextColor)
            Cell(result.versionType.label(), VersionWidth, MaterialTheme.typography.bodyMedium, SecondaryTextColor)
            Cell("${result.timeToFirstFrameMs} ms", ToStartWidth, numberStyle, TextColor)
            // Color only appears on problems.
            Cell(
                text = result.freezeCount.toString(),
                width = FreezesWidth,
                style = numberStyle,
                color = if (result.freezeCount > 0) ProblemColor else TextColor,
            )
            // Same drawing as the Player's quality line, just 3dp tall and without a playhead.
            QualityLine(model = row.qualityLine, thickness = 3.dp, modifier = Modifier.weight(1f))
            Cell(
                text = formatWhen(result.startedAtMillis),
                width = WhenWidth,
                style = numberStyle,
                color = SecondaryTextColor,
                modifier = Modifier.padding(start = 16.dp),
            )
        }
    }
}

@Composable
private fun RowScope.Cell(
    text: String,
    width: Dp,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = style,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.width(width),
    )
}

/** "16 Oct · 14:05" in the TV's time zone. */
private fun formatWhen(millis: Long): String =
    DateTimeFormatter.ofPattern("d MMM · HH:mm", Locale.getDefault())
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(millis))
