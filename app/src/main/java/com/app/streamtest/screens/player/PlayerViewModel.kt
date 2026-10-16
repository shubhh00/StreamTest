package com.app.streamtest.screens.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import com.app.streamtest.BuildConfig
import com.app.streamtest.Routes
import com.app.streamtest.domain.catalog.CatalogRepository
import com.app.streamtest.domain.catalog.StreamType
import com.app.streamtest.domain.catalog.findStream
import com.app.streamtest.player.LiveStats
import com.app.streamtest.player.PlayerFactory
import com.app.streamtest.player.QualityOption
import com.app.streamtest.player.StatsCollector
import com.app.streamtest.player.overrideFor
import com.app.streamtest.player.videoQualityOptions
import com.app.streamtest.ui.label
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
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
    /** Protected (DRM) stream: the start panel then waits for a "DRM license" step. */
    val isProtected: Boolean = false,
    /** ▲ on the remote shows/hides the stats overlay. */
    val statsVisible: Boolean = true,
    /** ▼ opens the Quality panel; BACK closes it. */
    val qualityPanelOpen: Boolean = false,
    /** Every video quality the stream offers, sharpest first. */
    val qualities: List<QualityOption> = emptyList(),
    /** The quality the user forced, or null for Auto. */
    val forcedQualityKey: String? = null,
)

// @HiltViewModel + @Inject constructor: Hilt builds this ViewModel and passes in what it needs.
// The player lives here (not in the composable) so it survives recomposition, and it is
// released exactly once, in onCleared(), when the user leaves the Player screen.
@OptIn(UnstableApi::class) // EventLogger is marked @UnstableApi in Media3.
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

    private val statsCollector = StatsCollector(player)

    /** Live numbers for the overlay. */
    val stats: StateFlow<LiveStats> = statsCollector.stats

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    init {
        statsCollector.attach()
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                // errorCodeName is a readable name such as "ERROR_CODE_IO_BAD_HTTP_STATUS".
                _state.update { it.copy(errorMessage = error.errorCodeName) }
            }

            // Called once the playlist/manifest is read (and again if the tracks change):
            // that's when we know which qualities exist.
            override fun onTracksChanged(tracks: Tracks) {
                _state.update { it.copy(qualities = tracks.videoQualityOptions()) }
            }
        })
        // EventLogger prints every Media3 event (loads, format changes, errors) to Logcat
        // under the tag "EventLogger". Very handy while learning; debug builds only.
        if (BuildConfig.DEBUG) player.addAnalyticsListener(EventLogger())

        viewModelScope.launch { startPlayback() }

        // Some values (buffered ahead, position) change all the time without an event,
        // so we read them twice a second. The loop stops when the ViewModel is cleared.
        viewModelScope.launch {
            while (true) {
                statsCollector.onTick()
                delay(500)
            }
        }
    }

    private suspend fun startPlayback() {
        val found = catalogRepository.getCatalog().findStream(streamId)
        if (found == null) {
            _state.update { it.copy(errorMessage = "Unknown stream: $streamId") }
            return
        }
        _state.update {
            it.copy(
                title = "${found.title} — ${found.stream.type.label()}",
                isProtected = found.stream.type == StreamType.DASH_DRM,
            )
        }

        player.setMediaItem(PlayerFactory.createMediaItem(found.stream))
        // Start-up times are measured from this moment.
        statsCollector.onPrepare()
        // prepare() starts the work: fetch the playlist/manifest, then the first chunks.
        player.prepare()
        // playWhenReady = "start playing as soon as enough video is buffered".
        player.playWhenReady = true
    }

    /** The app is visible again (e.g. back from the TV home screen): carry on playing. */
    fun onAppVisible() {
        player.playWhenReady = true
    }

    /** ▲ on the remote. */
    fun onToggleStats() {
        _state.update { it.copy(statsVisible = !it.statsVisible) }
    }

    /** ▼ on the remote. */
    fun onOpenQualityPanel() {
        _state.update { it.copy(qualityPanelOpen = true) }
    }

    /** BACK while the Quality panel is open. */
    fun onCloseQualityPanel() {
        _state.update { it.copy(qualityPanelOpen = false) }
    }

    /**
     * Forces one quality, or goes back to Auto when [key] is null.
     *
     * Auto = adaptive bitrate (ABR): the player measures how fast chunks download and
     * keeps choosing the best quality the network can sustain, switching up and down
     * as it changes. Forcing a quality turns that off for video.
     */
    fun onQualitySelected(key: String?) {
        val builder = player.trackSelectionParameters.buildUpon()
        if (key == null) {
            builder.clearOverridesOfType(C.TRACK_TYPE_VIDEO)
        } else {
            val override = player.currentTracks.overrideFor(key) ?: return
            builder.setOverrideForType(override)
        }
        // The player re-selects tracks right away and re-buffers from the current
        // position, so the new quality shows within a moment.
        player.trackSelectionParameters = builder.build()
        _state.update { it.copy(forcedQualityKey = key) }
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
