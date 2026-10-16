package com.app.streamtest.domain.results

import kotlin.coroutines.cancellation.CancellationException

/**
 * Saves [result] and reports whether it worked, instead of throwing. Used when leaving
 * the Player: a full disk or a database error must not crash the app on BACK.
 * (Cancellation is passed on: that's the coroutine being stopped, not a failure.)
 */
suspend fun ResultsRepository.saveSafely(result: TestResult): Boolean =
    try {
        save(result)
        true
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        false
    }
