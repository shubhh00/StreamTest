package com.app.streamtest.domain.results

/**
 * Groups results so the HLS / DASH / Protected tests of the same film sit together.
 * - Films are ordered by their most recent test (newest first).
 * - Inside a film: HLS, then DASH, then Protected; the newest first within each.
 */
fun groupByFilm(results: List<TestResult>): List<List<TestResult>> {
    val newestFirst = results.sortedByDescending { it.startedAtMillis }
    // groupBy keeps the order in which each film first appears, i.e. newest test first.
    return newestFirst.groupBy { it.filmTitle }.values.map { filmResults ->
        filmResults.sortedWith(
            compareBy<TestResult> { it.versionType.ordinal }.thenByDescending { it.startedAtMillis },
        )
    }
}
