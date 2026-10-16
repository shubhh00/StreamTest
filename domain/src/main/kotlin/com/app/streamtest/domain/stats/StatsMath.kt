package com.app.streamtest.domain.stats

import com.app.streamtest.domain.results.TestResult

/** "HD or better" = 720p and up. */
private const val HD_HEIGHT = 720

/**
 * Share of the watched time (0..100) spent at 720p or sharper.
 * Each quality lasts from its change until the next change (or [playedUntilMs]).
 */
fun timeAtHdOrBetterPercent(changes: List<QualityChange>, playedUntilMs: Long): Int {
    if (playedUntilMs <= 0 || changes.isEmpty()) return 0
    val sorted = changes.sortedBy { it.positionMs }
    var hdMs = 0L
    sorted.forEachIndexed { index, change ->
        val startMs = if (index == 0) 0L else change.positionMs
        val endMs = minOf(sorted.getOrNull(index + 1)?.positionMs ?: playedUntilMs, playedUntilMs)
        if (endMs > startMs && standardHeight(change.width, change.height) >= HD_HEIGHT) {
            hdMs += endMs - startMs
        }
    }
    return (hdMs * 100 / playedUntilMs).toInt()
}

/** The middle value (or the average of the two middle values). Null for an empty list. */
fun median(values: List<Long>): Long? {
    if (values.isEmpty()) return null
    val sorted = values.sorted()
    val middle = sorted.size / 2
    return if (sorted.size % 2 == 1) sorted[middle] else (sorted[middle - 1] + sorted[middle]) / 2
}

/** The one-line summary at the top of the Log. */
data class ResultsSummary(
    val medianTimeToStartMs: Long?,
    val freezesPerTest: Double,
    val hdOrBetterPercent: Int,
)

fun summarize(results: List<TestResult>): ResultsSummary {
    if (results.isEmpty()) return ResultsSummary(medianTimeToStartMs = null, freezesPerTest = 0.0, hdOrBetterPercent = 0)
    return ResultsSummary(
        // Median, not average: one very slow start shouldn't drag the number around.
        medianTimeToStartMs = median(results.map { it.timeToFirstFrameMs }),
        freezesPerTest = results.sumOf { it.freezeCount }.toDouble() / results.size,
        hdOrBetterPercent = results.sumOf { it.timeAtHdOrBetterPercent } / results.size,
    )
}
