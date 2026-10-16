package com.app.streamtest.screens.player

import com.app.streamtest.player.QualityOption
import org.junit.Assert.assertEquals
import org.junit.Test

class QualityRowTextTest {

    // The Tears of Steel HLS ladder: two different 1680×750 tracks.
    private val tearsHls = listOf(
        QualityOption(key = "0:4", width = 1680, height = 750, bitrate = 2_468_000),
        QualityOption(key = "0:3", width = 1680, height = 750, bitrate = 1_727_000),
        QualityOption(key = "0:2", width = 784, height = 350, bitrate = 1_197_000),
    )

    @Test
    fun `a name used twice gets the bitrate in its title`() {
        val texts = qualityRowTexts(tearsHls)

        assertEquals(QualityRowText("1080p · 2.5 Mbps", "1680×750"), texts[0])
        assertEquals(QualityRowText("1080p · 1.7 Mbps", "1680×750"), texts[1])
    }

    @Test
    fun `a unique name keeps the short title`() {
        val texts = qualityRowTexts(tearsHls)

        assertEquals(QualityRowText("480p", "784×350 · 1.2 Mbps"), texts[2])
    }
}
