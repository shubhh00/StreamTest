package com.app.streamtest.screens.player

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.app.streamtest.domain.stats.QualityBucket
import com.app.streamtest.domain.stats.QualityLineModel
import com.app.streamtest.domain.stats.QualitySegment
import com.app.streamtest.domain.stats.StartMilestones
import com.app.streamtest.player.LiveStats
import com.app.streamtest.ui.theme.BackgroundColor
import com.app.streamtest.ui.theme.ProblemColor
import com.app.streamtest.ui.theme.SafeAreaHorizontal
import com.app.streamtest.ui.theme.SafeAreaVertical
import com.app.streamtest.ui.theme.SecondaryTextColor
import com.app.streamtest.ui.theme.StreamTestTheme

// Root: gets the ViewModel from Hilt and passes plain values to the Screen.
@Composable
fun PlayerRoot(viewModel: PlayerViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    // Runs onAppVisible() when the screen is started (visible), and onAppHidden() when
    // it is stopped (HOME pressed, another app on top) or when we leave the screen.
    LifecycleStartEffect(viewModel) {
        viewModel.onAppVisible()
        onStopOrDispose { viewModel.onAppHidden() }
    }

    PlayerScreen(
        player = viewModel.player,
        state = state,
        stats = stats,
        onToggleStats = viewModel::onToggleStats,
        onOpenQualityPanel = viewModel::onOpenQualityPanel,
        onCloseQualityPanel = viewModel::onCloseQualityPanel,
        onQualitySelected = viewModel::onQualitySelected,
    )
}

// Full-screen video with our overlay on top. BACK is handled by the NavHost
// (or closes the Quality panel first, when it's open).
@Composable
fun PlayerScreen(
    player: Player?,
    state: PlayerState,
    stats: LiveStats,
    onToggleStats: () -> Unit,
    onOpenQualityPanel: () -> Unit,
    onCloseQualityPanel: () -> Unit,
    onQualitySelected: (key: String?) -> Unit,
) {
    // Remote keys go to whatever has focus. Normally nothing on this screen is a button,
    // so the whole screen takes focus itself (focusable + requestFocus) to receive ▲ / ▼.
    val screenFocus = remember { FocusRequester() }
    val qualityPanelFocus = remember { FocusRequester() }

    // When the Quality panel opens, focus jumps to its chosen row; when it closes,
    // focus goes back to the screen.
    LaunchedEffect(state.qualityPanelOpen) {
        if (state.qualityPanelOpen) qualityPanelFocus.requestFocus() else screenFocus.requestFocus()
    }

    // BackHandler catches BACK before the NavHost does, but only while it's enabled.
    BackHandler(enabled = state.qualityPanelOpen) { onCloseQualityPanel() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(screenFocus)
            .focusable()
            .onKeyEvent { event ->
                // Key events from the panel rows bubble up to here too. While the panel is
                // open we must not use ▲ / ▼, or the D-pad couldn't move inside the list.
                if (event.type != KeyEventType.KeyDown || state.qualityPanelOpen) return@onKeyEvent false
                when (event.key) {
                    Key.DirectionUp -> { onToggleStats(); true }
                    Key.DirectionDown -> { onOpenQualityPanel(); true }
                    else -> false // not ours (e.g. BACK): let the system handle it
                }
            },
    ) {
        if (player != null) VideoSurface(player = player)

        val error = state.errorMessage
        when {
            error != null -> ErrorMessage(
                error = error,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(PanelBackground)
                    .padding(24.dp),
            )
            else -> {
                if (state.statsVisible) StatsOverlay(state = state, stats = stats)
                if (state.qualityPanelOpen) {
                    QualityPanel(
                        qualities = state.qualities,
                        forcedQualityKey = state.forcedQualityKey,
                        stats = stats,
                        initialFocus = qualityPanelFocus,
                        onQualitySelected = onQualitySelected,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(horizontal = SafeAreaHorizontal, vertical = SafeAreaVertical)
                            .width(300.dp)
                            .background(PanelBackground)
                            .padding(vertical = 12.dp),
                    )
                }
            }
        }
    }
}

// A dark see-through backing so text stays readable over any video.
private val PanelBackground = BackgroundColor.copy(alpha = 0.85f)

@Composable
private fun StatsOverlay(state: PlayerState, stats: LiveStats) {
    val panelBackground = PanelBackground
    Box(modifier = Modifier.fillMaxSize()) {
        StartTimePanel(
            milestones = stats.milestones,
            isProtected = state.isProtected,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(horizontal = SafeAreaHorizontal, vertical = SafeAreaVertical)
                .width(300.dp)
                .background(panelBackground)
                .padding(16.dp),
        )
        StatsPanel(
            title = state.title,
            stats = stats,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(panelBackground)
                .padding(horizontal = SafeAreaHorizontal, vertical = SafeAreaVertical),
        )
    }
}

@Composable
private fun ErrorMessage(error: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = "Can't play this stream", style = MaterialTheme.typography.titleLarge, color = ProblemColor)
        Text(text = error, color = SecondaryTextColor)
    }
}

@Composable
private fun VideoSurface(player: Player) {
    // PlayerView (from media3-ui) is a classic Android View that draws the video.
    // AndroidView is the bridge that lets a classic View live inside Compose.
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                // Hide Media3's built-in controls (play/pause, seek bar). We draw our own
                // overlay in Compose, and the default controls would also steal D-pad focus.
                useController = false
                // Stops the TV from dimming or starting its screensaver during playback.
                keepScreenOn = true
                this.player = player
            }
        },
        // Detach the player when this view goes away; the ViewModel releases the player itself.
        onRelease = { playerView -> playerView.player = null },
        modifier = Modifier.fillMaxSize(),
    )
}

@Preview(device = Devices.TV_1080p)
@Composable
private fun PlayerScreenPreview() {
    val stats = LiveStats(
        videoWidth = 1920, videoHeight = 1080, codecs = "avc1.640028", bitrate = 4_200_000,
        bufferedAheadMs = 12_400, freezeCount = 1, freezeTotalMs = 900, droppedFrames = 3,
        positionMs = 95_000, durationMs = 734_000,
        milestones = StartMilestones(manifestLoadedMs = 420, firstVideoChunkMs = 1_320, firstFrameMs = 1_640),
        qualityLine = QualityLineModel(
            segments = listOf(
                QualitySegment(0f, 0.04f, QualityBucket.LOW),
                QualitySegment(0.04f, 0.13f, QualityBucket.HIGH),
            ),
            freezeFractions = listOf(0.09f),
        ),
        playheadFraction = 0.13f,
    )
    StreamTestTheme {
        PlayerScreen(
            player = null,
            state = PlayerState(title = "Tears of Steel — HLS"),
            stats = stats,
            onToggleStats = {},
            onOpenQualityPanel = {},
            onCloseQualityPanel = {},
            onQualitySelected = {},
        )
    }
}
