package com.app.streamtest.data.results

import com.app.streamtest.domain.catalog.StreamType
import com.app.streamtest.domain.results.PlaybackTimeline
import com.app.streamtest.domain.results.TestResult
import com.app.streamtest.domain.stats.Freeze
import com.app.streamtest.domain.stats.QualityChange
import com.app.streamtest.domain.stats.StartMilestones
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Converts between the domain TestResult and the Room row. The timeline and the
// milestones go into JSON text columns (see TestResultEntity).

@Serializable
internal class TimelineJson(
    val qualityChanges: List<QualityChangeJson> = emptyList(),
    val freezes: List<FreezeJson> = emptyList(),
    val playedUntilMs: Long = 0,
    val lengthMs: Long = 0,
)

@Serializable
internal class QualityChangeJson(val positionMs: Long, val width: Int, val height: Int)

@Serializable
internal class FreezeJson(val positionMs: Long, val durationMs: Long)

@Serializable
internal class MilestonesJson(
    val manifestLoadedMs: Long? = null,
    val drmKeysLoadedMs: Long? = null,
    val firstVideoChunkMs: Long? = null,
    val firstFrameMs: Long? = null,
)

private val json = Json { ignoreUnknownKeys = true }

fun TestResult.toEntity(): TestResultEntity = TestResultEntity(
    id = id,
    streamId = streamId,
    filmTitle = filmTitle,
    versionType = versionType.name,
    startedAtMillis = startedAtMillis,
    timeToFirstFrameMs = timeToFirstFrameMs,
    freezeCount = freezeCount,
    freezeTotalMs = freezeTotalMs,
    droppedFrames = droppedFrames,
    timeAtHdOrBetterPercent = timeAtHdOrBetterPercent,
    timelineJson = json.encodeToString(
        TimelineJson(
            qualityChanges = timeline.qualityChanges.map { QualityChangeJson(it.positionMs, it.width, it.height) },
            freezes = timeline.freezes.map { FreezeJson(it.positionMs, it.durationMs) },
            playedUntilMs = timeline.playedUntilMs,
            lengthMs = timeline.lengthMs,
        ),
    ),
    milestonesJson = json.encodeToString(
        MilestonesJson(
            manifestLoadedMs = milestones.manifestLoadedMs,
            drmKeysLoadedMs = milestones.drmKeysLoadedMs,
            firstVideoChunkMs = milestones.firstVideoChunkMs,
            firstFrameMs = milestones.firstFrameMs,
        ),
    ),
)

fun TestResultEntity.toTestResult(): TestResult {
    val timeline = json.decodeFromString<TimelineJson>(timelineJson)
    val milestones = json.decodeFromString<MilestonesJson>(milestonesJson)
    return TestResult(
        id = id,
        streamId = streamId,
        filmTitle = filmTitle,
        // An unknown name would mean a bug or a future version; fall back to DASH.
        versionType = StreamType.entries.firstOrNull { it.name == versionType } ?: StreamType.DASH,
        startedAtMillis = startedAtMillis,
        timeToFirstFrameMs = timeToFirstFrameMs,
        freezeCount = freezeCount,
        freezeTotalMs = freezeTotalMs,
        droppedFrames = droppedFrames,
        timeAtHdOrBetterPercent = timeAtHdOrBetterPercent,
        timeline = PlaybackTimeline(
            qualityChanges = timeline.qualityChanges.map { QualityChange(it.positionMs, it.width, it.height) },
            freezes = timeline.freezes.map { Freeze(it.positionMs, it.durationMs) },
            playedUntilMs = timeline.playedUntilMs,
            lengthMs = timeline.lengthMs,
        ),
        milestones = StartMilestones(
            manifestLoadedMs = milestones.manifestLoadedMs,
            drmKeysLoadedMs = milestones.drmKeysLoadedMs,
            firstVideoChunkMs = milestones.firstVideoChunkMs,
            firstFrameMs = milestones.firstFrameMs,
        ),
    )
}
