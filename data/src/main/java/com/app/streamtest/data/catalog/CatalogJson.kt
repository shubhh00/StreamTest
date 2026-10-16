package com.app.streamtest.data.catalog

import com.app.streamtest.domain.catalog.Catalog
import com.app.streamtest.domain.catalog.ExtraStream
import com.app.streamtest.domain.catalog.Film
import com.app.streamtest.domain.catalog.StreamType
import com.app.streamtest.domain.catalog.StreamVersion
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// These classes mirror the JSON file exactly. They stay inside :data;
// parseCatalog() turns them into the :domain models the rest of the app uses.

@Serializable
internal class CatalogJson(
    val films: List<FilmJson> = emptyList(),
    val extras: List<ExtraJson> = emptyList(),
)

@Serializable
internal class FilmJson(
    val id: String,
    val title: String,
    val description: String = "",
    val versions: List<VersionJson> = emptyList(),
)

@Serializable
internal class VersionJson(
    val id: String,
    val type: String,
    val url: String,
    val licenseUrl: String? = null,
    val subtitle: String? = null,
)

@Serializable
internal class ExtraJson(
    val id: String,
    val title: String,
    val type: String,
    val url: String,
    val note: String = "",
    val licenseUrl: String? = null,
)

private val json = Json { ignoreUnknownKeys = true }

/**
 * Turns the text of streams.json into a [Catalog].
 * A stream with an unknown "type", or a DASH_DRM stream without a "licenseUrl",
 * is skipped, so one bad entry doesn't break the whole library.
 */
fun parseCatalog(jsonText: String): Catalog {
    val catalogJson = json.decodeFromString<CatalogJson>(jsonText)
    return Catalog(
        films = catalogJson.films.map { film ->
            Film(
                id = film.id,
                title = film.title,
                description = film.description,
                versions = film.versions.mapNotNull { toStreamVersion(it.id, it.type, it.url, it.licenseUrl, it.subtitle) },
            )
        },
        extras = catalogJson.extras.mapNotNull { extra ->
            val stream = toStreamVersion(extra.id, extra.type, extra.url, extra.licenseUrl, subtitle = null)
            stream?.let { ExtraStream(title = extra.title, note = extra.note, stream = it) }
        },
    )
}

private fun toStreamVersion(
    id: String,
    type: String,
    url: String,
    licenseUrl: String?,
    subtitle: String?,
): StreamVersion? {
    val streamType = StreamType.entries.firstOrNull { it.name == type } ?: return null
    if (streamType == StreamType.DASH_DRM && licenseUrl.isNullOrBlank()) return null
    return StreamVersion(id = id, type = streamType, url = url, licenseUrl = licenseUrl, subtitle = subtitle)
}
