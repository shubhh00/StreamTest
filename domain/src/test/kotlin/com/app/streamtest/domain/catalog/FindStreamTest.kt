package com.app.streamtest.domain.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FindStreamTest {

    private val catalog = Catalog(
        films = listOf(
            Film(
                id = "tears",
                title = "Tears of Steel",
                description = "",
                versions = listOf(StreamVersion("tears-hls", StreamType.HLS, "https://a/t.m3u8")),
            ),
        ),
        extras = listOf(
            ExtraStream("Live", "Live stream", StreamVersion("live", StreamType.HLS, "https://a/l.m3u8")),
        ),
    )

    @Test
    fun `finds a film version with the film title`() {
        val found = catalog.findStream("tears-hls")
        assertEquals("Tears of Steel", found?.title)
        assertEquals(StreamType.HLS, found?.stream?.type)
    }

    @Test
    fun `finds an extra with the extra title`() {
        assertEquals("Live", catalog.findStream("live")?.title)
    }

    @Test
    fun `unknown id gives null`() {
        assertNull(catalog.findStream("nope"))
    }
}
