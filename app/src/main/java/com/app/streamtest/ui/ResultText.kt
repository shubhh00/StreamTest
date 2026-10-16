package com.app.streamtest.ui

import com.app.streamtest.domain.results.TestResult

/** The short result shown on a Library row: "640 ms to start" or "640 ms to start · 2 freezes". */
fun TestResult.shortSummary(): String {
    val start = "$timeToFirstFrameMs ms to start"
    return when (freezeCount) {
        0 -> start
        1 -> "$start · 1 freeze"
        else -> "$start · $freezeCount freezes"
    }
}

/** Result text for a row: the last saved result, or "Not tested yet". */
fun resultTextFor(streamId: String, lastResults: Map<String, TestResult>): String =
    lastResults[streamId]?.shortSummary() ?: NOT_TESTED_YET
