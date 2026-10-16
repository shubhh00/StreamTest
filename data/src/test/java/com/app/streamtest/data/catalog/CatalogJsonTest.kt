package com.app.streamtest.data.catalog

import com.app.streamtest.domain.catalog.StreamType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CatalogJsonTest {

    @Test
    fun `good file gives films, versions and extras`() {
        val catalog = parseCatalog(
            """
            {
              "films": [{
                "id": "tears", "title": "Tears of Steel", "description": "12 min",
                "versions": [
                  { "id": "tears-hls", "type": "HLS", "url": "https://a/x.m3u8" },
                  { "id": "tears-dash", "type": "DASH", "url": "https://a/x.mpd", "subtitle": "Adaptive" },
                  { "id": "tears-drm", "type": "DASH_DRM", "url": "https://a/y.mpd", "licenseUrl": "https://lic" }
                ]
              }],
              "extras": [{ "id": "live", "title": "Live", "type": "HLS", "url": "https://a/l.m3u8", "note": "Live" }]
            }
            """,
        )

        val film = catalog.films.single()
        assertEquals("Tears of Steel", film.title)
        assertEquals(listOf(StreamType.HLS, StreamType.DASH, StreamType.DASH_DRM), film.versions.map { it.type })
        assertEquals("Adaptive", film.version(StreamType.DASH)?.subtitle)
        assertEquals("https://lic", film.version(StreamType.DASH_DRM)?.licenseUrl)
        assertEquals("live", catalog.extras.single().stream.id)
    }

    @Test
    fun `missing version is simply absent`() {
        val catalog = parseCatalog(
            """
            { "films": [{ "id": "sintel", "title": "Sintel",
                "versions": [{ "id": "sintel-dash", "type": "DASH", "url": "https://a/s.mpd" }] }] }
            """,
        )

        val film = catalog.films.single()
        assertNull(film.version(StreamType.HLS))
        assertNull(film.version(StreamType.DASH_DRM))
        assertEquals("sintel-dash", film.version(StreamType.DASH)?.id)
    }

    @Test
    fun `unknown type is skipped, other versions are kept`() {
        val catalog = parseCatalog(
            """
            { "films": [{ "id": "f", "title": "F", "versions": [
                { "id": "f-bad", "type": "SMOOTH", "url": "https://a/f" },
                { "id": "f-hls", "type": "HLS", "url": "https://a/f.m3u8" }
            ] }] }
            """,
        )

        assertEquals(listOf("f-hls"), catalog.films.single().versions.map { it.id })
    }

    @Test
    fun `protected stream without license url is skipped`() {
        val catalog = parseCatalog(
            """
            { "films": [{ "id": "f", "title": "F", "versions": [
                { "id": "f-drm", "type": "DASH_DRM", "url": "https://a/f.mpd" }
            ] }] }
            """,
        )

        assertEquals(emptyList<String>(), catalog.films.single().versions.map { it.id })
    }
}
