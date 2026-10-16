package com.app.streamtest.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.streamtest.domain.stats.QualityBucket
import com.app.streamtest.domain.stats.QualityLineModel
import com.app.streamtest.ui.theme.LineColor
import com.app.streamtest.ui.theme.ProblemColor
import com.app.streamtest.ui.theme.QualityHighColor
import com.app.streamtest.ui.theme.QualityLowColor
import com.app.streamtest.ui.theme.QualityMidColor
import com.app.streamtest.ui.theme.TextColor

/**
 * The quality line: a thin bar that is also the progress bar.
 * - empty track in the line color,
 * - the played part in segments, brighter = sharper (480p dim, 720p mid, 1080p+ off-white),
 * - a dot in the problem color at each freeze,
 * - a small vertical line at the playhead (only when [playheadFraction] is given).
 * Used in the Player (4dp) and, later, the Log (3dp).
 */
@Composable
fun QualityLine(
    model: QualityLineModel,
    modifier: Modifier = Modifier,
    playheadFraction: Float? = null,
    thickness: Dp = 4.dp,
) {
    // With a playhead the canvas is taller, so the playhead mark can stick out above and below.
    val canvasHeight = if (playheadFraction != null) thickness * 3 else thickness
    Canvas(modifier = modifier.fillMaxWidth().height(canvasHeight)) {
        val lineThickness = thickness.toPx()
        val top = (size.height - lineThickness) / 2
        val centerY = size.height / 2

        drawRect(color = LineColor, topLeft = Offset(0f, top), size = Size(size.width, lineThickness))

        model.segments.forEach { segment ->
            drawRect(
                color = segment.bucket.color(),
                topLeft = Offset(segment.startFraction * size.width, top),
                size = Size((segment.endFraction - segment.startFraction) * size.width, lineThickness),
            )
        }
        model.freezeFractions.forEach { fraction ->
            drawCircle(color = ProblemColor, radius = lineThickness, center = Offset(fraction * size.width, centerY))
        }
        if (playheadFraction != null) {
            val playheadWidth = 2.dp.toPx()
            drawRect(
                color = TextColor,
                topLeft = Offset(playheadFraction * size.width - playheadWidth / 2, 0f),
                size = Size(playheadWidth, size.height),
            )
        }
    }
}

private fun QualityBucket.color(): Color = when (this) {
    QualityBucket.LOW -> QualityLowColor
    QualityBucket.MID -> QualityMidColor
    QualityBucket.HIGH -> QualityHighColor
}
