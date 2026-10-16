package com.app.streamtest.screens.log

import com.app.streamtest.domain.catalog.StreamType
import com.app.streamtest.domain.device.DeviceCapabilities
import com.app.streamtest.domain.device.DeviceInfoRepository
import com.app.streamtest.domain.results.PlaybackTimeline
import com.app.streamtest.domain.results.ResultsRepository
import com.app.streamtest.domain.results.TestResult
import com.app.streamtest.domain.stats.QualityChange
import com.app.streamtest.domain.stats.StartMilestones
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LogViewModelTest {

    private val resultsRepository = mockk<ResultsRepository>()
    private val deviceInfoRepository = mockk<DeviceInfoRepository>()

    @Before
    fun setUp() {
        // viewModelScope needs a Main dispatcher; see LibraryViewModelTest.
        Dispatchers.setMain(UnconfinedTestDispatcher())
        coEvery { deviceInfoRepository.getCapabilities() } returns DeviceCapabilities(
            deviceName = "Google TV Streamer",
            widevineLevel = "L1",
            decodes4k = true,
            hasDolbyVisionDecoder = true,
            hasHevcDecoder = true,
            hasAv1Decoder = true,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `rows are grouped by film, title only on the first row`() {
        every { resultsRepository.observeAll() } returns flowOf(
            listOf(
                result(id = 3, film = "Tears", type = StreamType.DASH, startedAt = 300),
                result(id = 2, film = "Bunny", type = StreamType.HLS, startedAt = 200),
                result(id = 1, film = "Tears", type = StreamType.HLS, startedAt = 100),
            ),
        )

        val rows = LogViewModel(resultsRepository, deviceInfoRepository).state.value.rows

        assertEquals(listOf(1L, 3L, 2L), rows.map { it.result.id }) // Tears HLS, Tears DASH, Bunny HLS
        assertEquals(listOf(true, false, true), rows.map { it.showFilmTitle })
    }

    @Test
    fun `header shows device, count and summary`() {
        every { resultsRepository.observeAll() } returns flowOf(
            listOf(
                result(id = 1, film = "Tears", type = StreamType.HLS, startedAt = 100, timeToStart = 1_000, freezes = 1),
                result(id = 2, film = "Tears", type = StreamType.DASH, startedAt = 200, timeToStart = 3_000, freezes = 0),
            ),
        )

        val state = LogViewModel(resultsRepository, deviceInfoRepository).state.value

        assertEquals("Google TV Streamer", state.deviceName)
        assertEquals(2, state.testCount)
        assertEquals(2_000L, state.summary?.medianTimeToStartMs)
        assertEquals(0.5, state.summary?.freezesPerTest ?: -1.0, 0.001)
    }

    @Test
    fun `each row gets a quality line for its timeline`() {
        every { resultsRepository.observeAll() } returns flowOf(listOf(result(id = 1, film = "Tears", type = StreamType.HLS, startedAt = 100)))

        val line = LogViewModel(resultsRepository, deviceInfoRepository).state.value.rows.single().qualityLine

        // Watched 25 s of a 100 s video, all in one quality.
        assertEquals(0.25f, line.segments.single().endFraction)
    }

    private fun result(
        id: Long,
        film: String,
        type: StreamType,
        startedAt: Long,
        timeToStart: Long = 1_000,
        freezes: Int = 0,
    ) = TestResult(
        id = id,
        streamId = "$film-$type",
        filmTitle = film,
        versionType = type,
        startedAtMillis = startedAt,
        timeToFirstFrameMs = timeToStart,
        freezeCount = freezes,
        freezeTotalMs = 0,
        droppedFrames = 0,
        timeAtHdOrBetterPercent = 0,
        timeline = PlaybackTimeline(
            qualityChanges = listOf(QualityChange(0, 1280, 720)),
            freezes = emptyList(),
            playedUntilMs = 25_000,
            lengthMs = 100_000,
        ),
        milestones = StartMilestones(),
    )
}
