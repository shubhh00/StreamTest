package com.app.streamtest.domain.device

import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceSummaryTest {

    @Test
    fun `high-end TV plays everything`() {
        val summary = DeviceCapabilities(
            widevineLevel = "L1",
            supports4k = true,
            supportsHdr10 = true,
            supportsDolbyVision = true,
            hasHevcDecoder = true,
            hasAv1Decoder = true,
        ).toSummary()

        assertEquals(listOf("Widevine L1", "4K", "HDR10", "HEVC", "AV1", "Dolby Vision"), summary.canPlay)
        assertEquals(emptyList<String>(), summary.cantPlay)
    }

    @Test
    fun `emulator without Widevine lists what is missing`() {
        val summary = DeviceCapabilities(
            widevineLevel = null,
            supports4k = false,
            supportsHdr10 = false,
            supportsDolbyVision = false,
            hasHevcDecoder = true,
            hasAv1Decoder = false,
        ).toSummary()

        assertEquals(listOf("HEVC"), summary.canPlay)
        assertEquals(listOf("Widevine", "4K", "HDR10", "AV1", "Dolby Vision"), summary.cantPlay)
    }
}
