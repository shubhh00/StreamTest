package com.app.streamtest.screens.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.app.streamtest.domain.catalog.ExtraStream
import com.app.streamtest.domain.catalog.Film
import com.app.streamtest.domain.results.TestResult
import com.app.streamtest.ui.resultTextFor
import com.app.streamtest.ui.components.StreamRow
import com.app.streamtest.ui.theme.DimTextColor
import com.app.streamtest.ui.theme.FilmTitleStyle
import com.app.streamtest.ui.theme.FocusCornerRadius
import com.app.streamtest.ui.theme.SecondaryTextColor
import com.app.streamtest.ui.theme.TabularNumbers
import com.app.streamtest.ui.theme.TextColor

/**
 * Left column: the films in large type, then the "More streams" rows.
 * verticalScroll makes the column scroll by itself when D-pad focus moves to an
 * item below the bottom edge (Compose "brings the focused item into view").
 */
@Composable
fun FilmColumn(
    films: List<Film>,
    extras: List<ExtraStream>,
    selection: LibrarySelection,
    filmFocusRequesters: List<FocusRequester>,
    streamFocusRequesters: Map<String, FocusRequester>,
    lastResults: Map<String, TestResult>,
    onFilmFocused: (index: Int) -> Unit,
    onExtraFocused: (index: Int) -> Unit,
    onOpenPlayer: (streamId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // rememberScrollState is "saveable": the scroll position survives a trip to the Player.
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        films.forEachIndexed { index, film ->
            // RIGHT (or OK) on a film jumps to the row of its first available version.
            val firstVersionFocus = film.firstAvailableVersion()?.let { streamFocusRequesters[it.id] }
                ?: FocusRequester.Default
            FilmRow(
                number = index + 1,
                film = film,
                isSelected = selection == LibrarySelection.FilmAt(index),
                focusRequester = filmFocusRequesters[index],
                rightTarget = firstVersionFocus,
                onFocused = { onFilmFocused(index) },
                onClick = { firstVersionFocus.requestFocus() },
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "More streams",
            style = MaterialTheme.typography.labelLarge,
            color = SecondaryTextColor,
            modifier = Modifier.padding(start = 12.dp, bottom = 4.dp),
        )
        extras.forEachIndexed { index, extra ->
            StreamRow(
                title = extra.title,
                subtitle = extra.note,
                result = resultTextFor(extra.stream.id, lastResults),
                onClick = { onOpenPlayer(extra.stream.id) },
                onFocused = { onExtraFocused(index) },
                modifier = Modifier
                    .focusRequester(streamFocusRequesters.getValue(extra.stream.id))
                    // Extras have nothing to the right to move into, so RIGHT does nothing.
                    .focusProperties { right = FocusRequester.Cancel },
            )
        }
    }
}

// A film is not a filled button like the other rows: the selected film is simply
// off-white and the others are dim grey (AGENTS.md §6.1).
@Composable
private fun FilmRow(
    number: Int,
    film: Film,
    isSelected: Boolean,
    focusRequester: FocusRequester,
    rightTarget: FocusRequester,
    onFocused: () -> Unit,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            // focusProperties overrides where the D-pad goes. Without it, RIGHT would
            // pick whatever item is geometrically nearest, which may not be the first version.
            .focusProperties { right = rightTarget }
            .onFocusChanged { if (it.isFocused) onFocused() },
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(FocusCornerRadius)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            pressedContainerColor = Color.Transparent,
            contentColor = if (isSelected) TextColor else DimTextColor,
            focusedContentColor = TextColor,
            pressedContentColor = TextColor,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "%02d".format(number),
                style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = TabularNumbers),
                modifier = Modifier.width(32.dp),
            )
            Text(text = film.title, style = FilmTitleStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
