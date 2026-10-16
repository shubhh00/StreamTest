package com.app.streamtest.screens.library

import com.app.streamtest.domain.catalog.Catalog
import com.app.streamtest.domain.catalog.CatalogRepository
import com.app.streamtest.domain.catalog.ExtraStream
import com.app.streamtest.domain.catalog.Film
import com.app.streamtest.domain.catalog.StreamType
import com.app.streamtest.domain.catalog.StreamVersion
import com.app.streamtest.domain.device.DeviceCapabilities
import com.app.streamtest.domain.device.DeviceInfoRepository
import com.app.streamtest.domain.results.PlaybackTimeline
import com.app.streamtest.domain.results.ResultsRepository
import com.app.streamtest.domain.results.TestResult
import com.app.streamtest.domain.stats.StartMilestones
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    // mockk<T>() makes a fake object; coEvery / every say what its functions return.
    private val catalogRepository = mockk<CatalogRepository>()
    private val deviceInfoRepository = mockk<DeviceInfoRepository>()
    private val resultsRepository = mockk<ResultsRepository>()

    // Stands in for Room: emitting a new map here is like saving a test.
    private val latestResults = MutableStateFlow<Map<String, TestResult>>(emptyMap())

    private val tears = Film(
        id = "tears",
        title = "Tears of Steel",
        description = "12 min",
        versions = listOf(StreamVersion("tears-hls", StreamType.HLS, "https://a/t.m3u8")),
    )
    private val live = ExtraStream("Live", "Live stream", StreamVersion("live", StreamType.HLS, "https://a/l.m3u8"))

    @Before
    fun setUp() {
        // viewModelScope runs on Dispatchers.Main, which doesn't exist in a plain JVM
        // test. UnconfinedTestDispatcher replaces it and runs coroutines right away.
        Dispatchers.setMain(UnconfinedTestDispatcher())

        coEvery { catalogRepository.getCatalog() } returns Catalog(films = listOf(tears), extras = listOf(live))
        coEvery { deviceInfoRepository.getCapabilities() } returns DeviceCapabilities(
            widevineLevel = "L1",
            supports4k = true,
            supportsHdr10 = false,
            supportsDolbyVision = false,
            hasHevcDecoder = true,
            hasAv1Decoder = false,
        )
        every { resultsRepository.observeLatestPerStream() } returns latestResults
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = LibraryViewModel(catalogRepository, deviceInfoRepository, resultsRepository)

    @Test
    fun `loads films, extras and the device footer`() {
        val state = createViewModel().state.value

        assertEquals(listOf(tears), state.films)
        assertEquals(listOf(live), state.extras)
        assertEquals(listOf("Widevine L1", "4K", "HEVC"), state.deviceSummary?.canPlay)
        assertEquals(listOf("HDR10", "AV1", "Dolby Vision"), state.deviceSummary?.cantPlay)
    }

    @Test
    fun `a saved result shows up without reloading`() {
        val viewModel = createViewModel()
        assertEquals(emptyMap<String, TestResult>(), viewModel.state.value.lastResults)

        latestResults.value = mapOf("tears-hls" to result("tears-hls"))

        assertEquals(1_640L, viewModel.state.value.lastResults["tears-hls"]?.timeToFirstFrameMs)
    }

    @Test
    fun `focus changes the selected film or extra`() {
        val viewModel = createViewModel()

        viewModel.onExtraFocused(0)
        assertEquals(LibrarySelection.ExtraAt(0), viewModel.state.value.selection)

        viewModel.onFilmFocused(0)
        assertEquals(LibrarySelection.FilmAt(0), viewModel.state.value.selection)
    }

    @Test
    fun `remembers the opened row for focus on return`() {
        val viewModel = createViewModel()
        viewModel.onStreamOpened("tears-hls")
        assertEquals("tears-hls", viewModel.state.value.lastOpenedStreamId)
    }

    private fun result(streamId: String) = TestResult(
        streamId = streamId,
        filmTitle = "Tears of Steel",
        versionType = StreamType.HLS,
        startedAtMillis = 0,
        timeToFirstFrameMs = 1_640,
        freezeCount = 0,
        freezeTotalMs = 0,
        droppedFrames = 0,
        timeAtHdOrBetterPercent = 100,
        timeline = PlaybackTimeline(emptyList(), emptyList(), 0, 0),
        milestones = StartMilestones(),
    )
}
