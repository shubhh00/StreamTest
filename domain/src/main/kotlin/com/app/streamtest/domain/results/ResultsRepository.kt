package com.app.streamtest.domain.results

import kotlinx.coroutines.flow.Flow

/** Saves and reads test results. Implemented in :data with Room. */
interface ResultsRepository {
    suspend fun save(result: TestResult)

    /** Every result, newest first. The Flow sends a new list whenever one is saved. */
    fun observeAll(): Flow<List<TestResult>>

    /** The newest result for each stream, keyed by stream id (for the Library rows). */
    fun observeLatestPerStream(): Flow<Map<String, TestResult>>

    /** Deletes every saved result (Settings → Saved results). */
    suspend fun deleteAll()
}
