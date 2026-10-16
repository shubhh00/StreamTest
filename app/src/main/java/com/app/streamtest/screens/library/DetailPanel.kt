package com.app.streamtest.screens.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.app.streamtest.domain.catalog.ExtraStream
import com.app.streamtest.domain.catalog.Film
import com.app.streamtest.domain.catalog.StreamType
import com.app.streamtest.domain.catalog.StreamVersion
import com.app.streamtest.ui.defaultSubtitle
import com.app.streamtest.ui.label
import com.app.streamtest.ui.NOT_TESTED_YET
import com.app.streamtest.ui.components.StreamRow
import com.app.streamtest.ui.components.UnavailableStreamRow
import com.app.streamtest.ui.theme.DimTextColor
import com.app.streamtest.ui.theme.SecondaryTextColor
import java.net.URI

/** Right column for a film: its description, then one row per version (HLS, DASH, Protected). */
@Composable
fun FilmDetailPanel(
    film: Film,
    streamFocusRequesters: Map<String, FocusRequester>,
    filmFocus: FocusRequester,
    onOpenPlayer: (streamId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = film.description,
            style = MaterialTheme.typography.bodyLarge,
            color = SecondaryTextColor,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        Spacer(Modifier.height(16.dp))
        StreamType.entries.forEach { type ->
            val version = film.version(type)
            if (version == null) {
                UnavailableStreamRow(title = type.label())
            } else {
                StreamRow(
                    title = type.label(),
                    subtitle = version.subtitle ?: type.defaultSubtitle(),
                    result = NOT_TESTED_YET,
                    onClick = { onOpenPlayer(version.id) },
                    modifier = Modifier
                        // Lets code focus this exact row: RIGHT from the film, or back from the Player.
                        .focusRequester(streamFocusRequesters.getValue(version.id))
                        // LEFT from any version goes back to the film it belongs to.
                        .focusProperties { left = filmFocus },
                )
            }
        }
    }
}

/** The first version in display order (HLS, DASH, Protected) that this film has. */
fun Film.firstAvailableVersion(): StreamVersion? =
    StreamType.entries.firstNotNullOfOrNull { version(it) }

/** Right column for a "More streams" item: just information, nothing focusable. */
@Composable
fun ExtraDetailPanel(extra: ExtraStream, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(horizontal = 12.dp)) {
        Text(text = extra.title, style = MaterialTheme.typography.headlineSmall)
        Text(text = extra.note, style = MaterialTheme.typography.bodyLarge, color = SecondaryTextColor)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "${extra.stream.type.label()} · ${URI(extra.stream.url).host}",
            style = MaterialTheme.typography.bodySmall,
            color = SecondaryTextColor,
        )
        Text(text = "Press OK to play", style = MaterialTheme.typography.bodySmall, color = DimTextColor)
    }
}
