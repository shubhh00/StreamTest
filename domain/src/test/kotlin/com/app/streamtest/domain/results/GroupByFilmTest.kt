package com.app.streamtest.domain.results

import com.app.streamtest.domain.catalog.StreamType
import com.app.streamtest.domain.stats.StartMilestones
import org.junit.Assert.assertEquals
import org.junit.Test

class GroupByFilmTest {

    @Test
    fun `versions of a film sit together, most recently tested film first`() {
        val groups = groupByFilm(
            listOf(
                result("Tears", StreamType.DASH_DRM, startedAt = 1),
                result("Bunny", StreamType.HLS, startedAt = 2),
                result("Tears", StreamType.HLS, startedAt = 3),
                result("Tears", StreamType.DASH, startedAt = 4),
                result("Tears", StreamType.HLS, startedAt = 5),
            ),
        )

        assertEquals(listOf("Tears", "Bunny"), groups.map { it.first().filmTitle })
        // Inside Tears: HLS (newest first), then DASH, then Protected.
        assertEquals(
            listOf(StreamType.HLS to 5L, StreamType.HLS to 3L, StreamType.DASH to 4L, StreamType.DASH_DRM to 1L),
            groups.first().map { it.versionType to it.startedAtMillis },
        )
    }

    @Test
    fun `no results, no groups`() {
        assertEquals(emptyList<List<TestResult>>(), groupByFilm(emptyList()))
    }

    private fun result(film: String, type: StreamType, startedAt: Long) = TestResult(
        streamId = "$film-$type",
        filmTitle = film,
        versionType = type,
        startedAtMillis = startedAt,
        timeToFirstFrameMs = 1_000,
        freezeCount = 0,
        freezeTotalMs = 0,
        droppedFrames = 0,
        timeAtHdOrBetterPercent = 0,
        timeline = PlaybackTimeline(emptyList(), emptyList(), 0, 0),
        milestones = StartMilestones(),
    )
}
