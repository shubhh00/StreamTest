package com.app.streamtest.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.app.streamtest.ui.theme.ProblemColor
import com.app.streamtest.ui.theme.SafeAreaHorizontal
import com.app.streamtest.ui.theme.SafeAreaVertical
import com.app.streamtest.ui.theme.SecondaryTextColor
import com.app.streamtest.ui.theme.StreamTestTheme

// Root: gets the ViewModel from Hilt and passes plain values to the Screen.
@Composable
fun PlayerRoot(viewModel: PlayerViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Runs onAppVisible() when the screen is started (visible), and onAppHidden() when
    // it is stopped (HOME pressed, another app on top) or when we leave the screen.
    LifecycleStartEffect(viewModel) {
        viewModel.onAppVisible()
        onStopOrDispose { viewModel.onAppHidden() }
    }

    PlayerScreen(player = viewModel.player, state = state)
}

// Full-screen video. BACK is handled by the NavHost and returns to the Library.
// The stats overlay arrives in Milestone 4.
@Composable
fun PlayerScreen(player: Player?, state: PlayerState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (player != null) VideoSurface(player = player)

        val error = state.errorMessage
        if (error != null) {
            Column(modifier = Modifier.align(Alignment.Center)) {
                Text(text = "Can't play this stream", style = MaterialTheme.typography.titleLarge, color = ProblemColor)
                Text(text = error, color = SecondaryTextColor)
            }
        } else {
            Text(
                text = state.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = SafeAreaHorizontal, vertical = SafeAreaVertical),
            )
        }
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
    StreamTestTheme {
        PlayerScreen(player = null, state = PlayerState(title = "Tears of Steel — HLS"))
    }
}
