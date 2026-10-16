package com.app.streamtest.domain.results

import com.app.streamtest.domain.catalog.StreamType
import com.app.streamtest.domain.stats.StartMilestones
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class SaveSafelyTest {

    /** A tiny stand-in repository whose save() either works or throws. */
    private class TestRepository(private val failWith: Exception? = null) : ResultsRepository {
        val saved = mutableListOf<TestResult>()
        override suspend fun save(result: TestResult) {
            failWith?.let { throw it }
            saved.add(result)
        }
        override fun observeAll(): Flow<List<TestResult>> = emptyFlow()
        override fun observeLatestPerStream(): Flow<Map<String, TestResult>> = emptyFlow()
        override suspend fun deleteAll() = Unit
    }

    private val result = TestResult(
        streamId = "s",
        filmTitle = "Film",
        versionType = StreamType.HLS,
        startedAtMillis = 0,
        timeToFirstFrameMs = null,
        freezeCount = 0,
        freezeTotalMs = 0,
        droppedFrames = 0,
        timeAtHdOrBetterPercent = 0,
        timeline = PlaybackTimeline(emptyList(), emptyList(), 0, 0),
        milestones = StartMilestones(),
        outcome = TestOutcome.FAILED_TO_START,
        errorCode = "ERROR_CODE_IO_BAD_HTTP_STATUS",
    )

    @Test
    fun `a working save returns true`() = runBlocking {
        val repository = TestRepository()
        assertTrue(repository.saveSafely(result))
        assertEquals(listOf(result), repository.saved)
    }

    @Test
    fun `a failing save returns false instead of crashing`() = runBlocking {
        assertFalse(TestRepository(failWith = IllegalStateException("disk full")).saveSafely(result))
    }

    @Test(expected = CancellationException::class)
    fun `cancellation is passed on, not swallowed`() {
        runBlocking { TestRepository(failWith = CancellationException("left the screen")).saveSafely(result) }
    }
}
