package com.app.streamtest.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Surface
import com.app.streamtest.domain.catalog.ExtraStream
import com.app.streamtest.domain.catalog.Film
import com.app.streamtest.domain.catalog.StreamType
import com.app.streamtest.domain.catalog.StreamVersion
import com.app.streamtest.domain.device.DeviceSummary
import com.app.streamtest.ui.components.AppHeader
import com.app.streamtest.ui.components.HeaderTab
import com.app.streamtest.ui.theme.LineColor
import com.app.streamtest.ui.theme.SafeAreaHorizontal
import com.app.streamtest.ui.theme.SafeAreaVertical
import com.app.streamtest.ui.theme.StreamTestTheme

// Root: gets the ViewModel from Hilt and passes plain state + callbacks to the Screen.
@Composable
fun LibraryRoot(
    onOpenPlayer: (streamId: String) -> Unit,
    onOpenLog: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LibraryScreen(
        state = state,
        onFilmFocused = viewModel::onFilmFocused,
        onExtraFocused = viewModel::onExtraFocused,
        onOpenPlayer = { streamId ->
            viewModel.onStreamOpened(streamId)
            onOpenPlayer(streamId)
        },
        onOpenLog = onOpenLog,
    )
}

@Composable
fun LibraryScreen(
    state: LibraryState,
    onFilmFocused: (index: Int) -> Unit,
    onExtraFocused: (index: Int) -> Unit,
    onOpenPlayer: (streamId: String) -> Unit,
    onOpenLog: () -> Unit,
) {
    // A FocusRequester is a handle for moving focus to one item from code.
    // One per film, and one per playable row (film versions and extras), keyed by stream id.
    val filmFocusRequesters = remember(state.films) { List(state.films.size) { FocusRequester() } }
    val streamFocusRequesters = remember(state.films, state.extras) {
        val ids = state.films.flatMap { film -> film.versions.map { it.id } } + state.extras.map { it.stream.id }
        ids.associateWith { FocusRequester() }
    }

    // Where focus goes when this screen appears:
    // - first visit: the first film;
    // - back from the Player: the exact row that was opened. Leaving for the Player
    //   removes this screen from composition, but the ViewModel (and lastOpenedStreamId
    //   in it) stays alive, so when the screen comes back this effect runs again and
    //   focuses that row's FocusRequester.
    LaunchedEffect(state.films.isNotEmpty()) {
        if (state.films.isEmpty()) return@LaunchedEffect
        val openedRow = state.lastOpenedStreamId?.let { streamFocusRequesters[it] }
        (openedRow ?: filmFocusRequesters.first()).requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = SafeAreaHorizontal, vertical = SafeAreaVertical),
    ) {
        AppHeader(selectedTab = HeaderTab.Library, onLibraryClick = {}, onLogClick = onOpenLog)
        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            FilmColumn(
                films = state.films,
                extras = state.extras,
                selection = state.selection,
                filmFocusRequesters = filmFocusRequesters,
                streamFocusRequesters = streamFocusRequesters,
                onFilmFocused = onFilmFocused,
                onExtraFocused = onExtraFocused,
                onOpenPlayer = onOpenPlayer,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            Spacer(Modifier.width(40.dp))
            val panelModifier = Modifier.weight(1f).fillMaxHeight()
            when (val selection = state.selection) {
                is LibrarySelection.FilmAt -> state.films.getOrNull(selection.index)?.let { film ->
                    FilmDetailPanel(
                        film = film,
                        streamFocusRequesters = streamFocusRequesters,
                        filmFocus = filmFocusRequesters[selection.index],
                        onOpenPlayer = onOpenPlayer,
                        modifier = panelModifier,
                    )
                }
                is LibrarySelection.ExtraAt -> state.extras.getOrNull(selection.index)?.let { extra ->
                    ExtraDetailPanel(extra = extra, modifier = panelModifier)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(LineColor))
        Spacer(Modifier.height(12.dp))
        DeviceFooter(summary = state.deviceSummary)
    }
}

@Preview(device = Devices.TV_1080p)
@Composable
private fun LibraryScreenPreview() {
    val tears = Film(
        id = "tears",
        title = "Tears of Steel",
        description = "12 min · open movie by the Blender Foundation",
        versions = listOf(
            StreamVersion("tears-hls", StreamType.HLS, "https://example.com/t.m3u8", subtitle = "Adaptive · up to 1680×750"),
            StreamVersion("tears-dash", StreamType.DASH, "https://example.com/t.mpd", subtitle = "Adaptive · up to 1920×856"),
        ),
    )
    val state = LibraryState(
        isLoading = false,
        films = listOf(tears, tears.copy(id = "bbb", title = "Big Buck Bunny")),
        extras = listOf(
            ExtraStream("Akamai live stream", "Live 1080p · no fixed end",
                StreamVersion("live", StreamType.HLS, "https://example.com/live.m3u8")),
        ),
        deviceSummary = DeviceSummary(canPlay = listOf("Widevine L1", "4K", "HEVC"), cantPlay = listOf("AV1")),
    )
    StreamTestTheme {
        Surface {
            LibraryScreen(state = state, onFilmFocused = {}, onExtraFocused = {}, onOpenPlayer = {}, onOpenLog = {})
        }
    }
}
