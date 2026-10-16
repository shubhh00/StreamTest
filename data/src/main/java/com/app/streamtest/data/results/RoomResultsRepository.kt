package com.app.streamtest.data.results

import com.app.streamtest.domain.results.ResultsRepository
import com.app.streamtest.domain.results.TestResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Saves and reads results in the Room database. */
@Singleton
class RoomResultsRepository @Inject constructor(
    private val dao: TestResultDao,
) : ResultsRepository {

    override suspend fun save(result: TestResult) {
        dao.insert(result.toEntity())
    }

    override fun observeAll(): Flow<List<TestResult>> =
        dao.observeAll().map { rows -> rows.map { it.toTestResult() } }

    override fun observeLatestPerStream(): Flow<Map<String, TestResult>> =
        dao.observeLatestPerStream().map { rows -> rows.map { it.toTestResult() }.associateBy { it.streamId } }
}
