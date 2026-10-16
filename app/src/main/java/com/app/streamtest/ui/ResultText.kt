package com.app.streamtest.ui

import com.app.streamtest.domain.results.QualityMode
import com.app.streamtest.domain.results.TestOutcome
import com.app.streamtest.domain.results.TestResult

/**
 * The short result on a Library row, e.g. "640 ms to start · 2 freezes",
 * "Didn't start · bad http status", or "… · forced 720p".
 */
fun TestResult.shortSummary(): String {
    val startMs = timeToFirstFrameMs ?: return "Didn't start · ${shortErrorName(errorCode)}"
    val parts = mutableListOf("$startMs ms to start")
    when (freezeCount) {
        0 -> Unit
        1 -> parts.add("1 freeze")
        else -> parts.add("$freezeCount freezes")
    }
    if (outcome == TestOutcome.FAILED_WHILE_PLAYING) parts.add("stopped: ${shortErrorName(errorCode)}")
    if (qualityMode == QualityMode.FORCED) parts.add(forcedHeight?.let { "forced ${it}p" } ?: "forced")
    return parts.joinToString(" · ")
}

/** "ERROR_CODE_IO_BAD_HTTP_STATUS" → "io bad http status". */
fun shortErrorName(errorCode: String?): String =
    errorCode?.removePrefix("ERROR_CODE_")?.lowercase()?.replace('_', ' ') ?: "error"

/** Result text for a row: the last saved result, or "Not tested yet". */
fun resultTextFor(streamId: String, lastResults: Map<String, TestResult>): String =
    lastResults[streamId]?.shortSummary() ?: NOT_TESTED_YET

/** The last test of this stream failed: its row text is shown in the problem color. */
fun lastTestFailed(streamId: String, lastResults: Map<String, TestResult>): Boolean =
    lastResults[streamId]?.let { it.outcome != TestOutcome.PLAYED } ?: false
