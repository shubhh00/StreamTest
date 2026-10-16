package com.app.streamtest.screens.settings

import com.app.streamtest.domain.catalog.StreamType
import com.app.streamtest.domain.device.DecoderInfo
import com.app.streamtest.domain.device.DeviceDetails
import com.app.streamtest.domain.device.DeviceInfoRepository
import com.app.streamtest.domain.results.PlaybackTimeline
import com.app.streamtest.domain.results.ResultsRepository
import com.app.streamtest.domain.results.TestResult
import com.app.streamtest.domain.stats.StartMilestones
import io.mockk.coEvery
import io.mockk.coVerify
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
class SettingsViewModelTest {

    private val deviceInfoRepository = mockk<DeviceInfoRepository>()
    // relaxed = true: functions we don't set up (like deleteAll) just do nothing.
    private val resultsRepository = mockk<ResultsRepository>(relaxed = true)

    @Before
    fun setUp() {
        // viewModelScope needs a Main dispatcher; see LibraryViewModelTest.
        Dispatchers.setMain(UnconfinedTestDispatcher())
        coEvery { deviceInfoRepository.getDetails() } returns DeviceDetails(
            deviceName = "Test TV",
            androidVersion = "Android 14 (API 34)",
            widevineLevel = "L1",
            widevineVersion = null,
            maxHdcpLevel = null,
            videoDecoders = listOf(
                DecoderInfo("hw.hevc", "video/hevc", isHardware = true),
                DecoderInfo("sw.hevc", "video/hevc", isHardware = false),
                DecoderInfo("sw.avc", "video/avc", isHardware = false),
            ),
            displayModes = listOf("1920×1080 @ 60 Hz"),
            hdrTypes = emptyList(),
        )
        every { resultsRepository.observeAll() } returns flowOf(listOf(result(), result()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `decoders are grouped per format with a hardware count`() {
        val groups = SettingsViewModel(deviceInfoRepository, resultsRepository).state.value.decoderGroups

        assertEquals(listOf("video/avc", "video/hevc"), groups.map { it.mimeType })
        assertEquals(listOf(0, 1), groups.map { it.hardwareCount })
    }

    @Test
    fun `clearing needs two presses`() {
        val viewModel = SettingsViewModel(deviceInfoRepository, resultsRepository)
        assertEquals(2, viewModel.state.value.savedCount)

        viewModel.onClearPressed()
        assertEquals(ClearStep.CONFIRM, viewModel.state.value.clearStep)
        coVerify(exactly = 0) { resultsRepository.deleteAll() }

        viewModel.onClearPressed()
        assertEquals(ClearStep.DONE, viewModel.state.value.clearStep)
        coVerify(exactly = 1) { resultsRepository.deleteAll() }
    }

    @Test
    fun `moving to another section cancels a half-done clear`() {
        val viewModel = SettingsViewModel(deviceInfoRepository, resultsRepository)

        viewModel.onClearPressed()
        viewModel.onSectionFocused(SettingsSection.ABOUT)
        viewModel.onClearPressed()

        // Back to step one: still asking, nothing deleted.
        assertEquals(ClearStep.CONFIRM, viewModel.state.value.clearStep)
        coVerify(exactly = 0) { resultsRepository.deleteAll() }
    }

    private fun result() = TestResult(
        streamId = "s",
        filmTitle = "Film",
        versionType = StreamType.HLS,
        startedAtMillis = 0,
        timeToFirstFrameMs = 1_000,
        freezeCount = 0,
        freezeTotalMs = 0,
        droppedFrames = 0,
        timeAtHdOrBetterPercent = 0,
        timeline = PlaybackTimeline(emptyList(), emptyList(), 0, 0),
        milestones = StartMilestones(),
    )
}
