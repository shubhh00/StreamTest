package com.app.streamtest.screens.player

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import com.app.streamtest.BuildConfig
import com.app.streamtest.Routes
import com.app.streamtest.domain.catalog.CatalogRepository
import com.app.streamtest.domain.catalog.findStream
import com.app.streamtest.player.PlayerFactory
import com.app.streamtest.ui.label
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerState(
    /** e.g. "Tears of Steel — HLS". */
    val title: String = "",
    /** Set when the stream can't be found or Media3 reports a playback error. */
    val errorMessage: String? = null,
)

// @HiltViewModel + @Inject constructor: Hilt builds this ViewModel and passes in what it needs.
// The player lives here (not in the composable) so it survives recomposition, and it is
// released exactly once, in onCleared(), when the user leaves the Player screen.
@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext context: Context,
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    // Navigation copies route arguments (the {streamId} in "player/{streamId}")
    // into SavedStateHandle, so the ViewModel can read which stream to play.
    private val streamId: String = checkNotNull(savedStateHandle[Routes.STREAM_ID_ARG])

    val player: ExoPlayer = PlayerFactory.createPlayer(context)

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    init {
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                // errorCodeName is a readable name such as "ERROR_CODE_IO_BAD_HTTP_STATUS".
                _state.update { it.copy(errorMessage = error.errorCodeName) }
            }
        })
        // EventLogger prints every Media3 event (loads, format changes, errors) to Logcat
        // under the tag "EventLogger". Very handy while learning; debug builds only.
        if (BuildConfig.DEBUG) player.addAnalyticsListener(EventLogger())

        viewModelScope.launch { startPlayback() }
    }

    private suspend fun startPlayback() {
        val found = catalogRepository.getCatalog().findStream(streamId)
        if (found == null) {
            _state.update { it.copy(errorMessage = "Unknown stream: $streamId") }
            return
        }
        _state.update { it.copy(title = "${found.title} — ${found.stream.type.label()}") }

        player.setMediaItem(PlayerFactory.createMediaItem(found.stream))
        // prepare() starts the work: fetch the playlist/manifest, then the first chunks.
        player.prepare()
        // playWhenReady = "start playing as soon as enough video is buffered".
        player.playWhenReady = true
    }

    /** The app is visible again (e.g. back from the TV home screen): carry on playing. */
    fun onAppVisible() {
        player.playWhenReady = true
    }

    /** The app went to the background (HOME on the remote): don't keep playing unseen. */
    fun onAppHidden() {
        player.playWhenReady = false
    }

    override fun onCleared() {
        // Frees the decoders, network connections and buffers. A player that isn't
        // released keeps holding the hardware video decoder, which TVs have very few of.
        player.release()
    }
}
