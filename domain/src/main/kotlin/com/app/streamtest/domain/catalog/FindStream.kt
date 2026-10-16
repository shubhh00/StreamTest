package com.app.streamtest.domain.catalog

/** A stream found by its id, plus the film (or extra) title to show with it. */
data class FoundStream(
    val title: String,
    val stream: StreamVersion,
)

/** Looks a stream id up in the films first, then in the extras. Null if it's not there. */
fun Catalog.findStream(streamId: String): FoundStream? {
    for (film in films) {
        val version = film.versions.firstOrNull { it.id == streamId }
        if (version != null) return FoundStream(title = film.title, stream = version)
    }
    val extra = extras.firstOrNull { it.stream.id == streamId } ?: return null
    return FoundStream(title = extra.title, stream = extra.stream)
}
