package com.app.streamtest.domain.device

import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceSummaryTest {

    @Test
    fun `high-end TV plays everything`() {
        val summary = DeviceCapabilities(
            widevineLevel = "L1",
            decodes4k = true,
            hasHevcDecoder = true,
            hasAv1Decoder = true,
            hasDolbyVisionDecoder = true,
            screenMaxHeight = 2160,
            screenHdrTypes = listOf("HDR10", "HLG"),
        ).toSummary()

        assertEquals(listOf("Widevine L1", "4K", "HEVC", "AV1", "Dolby Vision"), summary.canPlay)
        assertEquals(emptyList<String>(), summary.cantPlay)
        assertEquals("2160p screen · HDR10, HLG", summary.screen)
    }

    @Test
    fun `decoding 4K on a 1080p screen is still decoding 4K`() {
        // The emulator case: the decoder handled 3840×2160 while the screen is 1080p.
        val summary = DeviceCapabilities(
            widevineLevel = "L3",
            decodes4k = true,
            hasHevcDecoder = true,
            hasAv1Decoder = true,
            hasDolbyVisionDecoder = false,
            screenMaxHeight = 1080,
        ).toSummary()

        assertEquals(listOf("Widevine L3", "4K", "HEVC", "AV1"), summary.canPlay)
        assertEquals(listOf("Dolby Vision"), summary.cantPlay)
        assertEquals("1080p screen · SDR", summary.screen)
    }

    @Test
    fun `device without Widevine lists what is missing`() {
        val summary = DeviceCapabilities(
            widevineLevel = null,
            decodes4k = false,
            hasHevcDecoder = true,
            hasAv1Decoder = false,
            hasDolbyVisionDecoder = false,
        ).toSummary()

        assertEquals(listOf("HEVC"), summary.canPlay)
        assertEquals(listOf("Widevine", "4K", "AV1", "Dolby Vision"), summary.cantPlay)
        assertEquals("Screen · SDR", summary.screen)
    }
}
