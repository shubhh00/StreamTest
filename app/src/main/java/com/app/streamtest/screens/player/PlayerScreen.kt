package com.app.streamtest.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.app.streamtest.ui.theme.SecondaryTextColor
import com.app.streamtest.ui.theme.StreamTestTheme

// Root: gets the ViewModel from Hilt and passes plain values to the Screen.
@Composable
fun PlayerRoot(viewModel: PlayerViewModel = hiltViewModel()) {
    PlayerScreen(streamId = viewModel.streamId)
}

// Milestone 1: a black full-screen placeholder. Video and stats arrive in
// Milestones 3 and 4. BACK is handled by the NavHost and returns to the Library.
@Composable
fun PlayerScreen(streamId: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Player", style = MaterialTheme.typography.headlineLarge)
        Text(text = "Stream: $streamId", color = SecondaryTextColor)
        Text(text = "Press BACK to return to the Library.", color = SecondaryTextColor)
    }
}

@Preview(device = Devices.TV_1080p)
@Composable
private fun PlayerScreenPreview() {
    StreamTestTheme {
        PlayerScreen(streamId = "tears-hls")
    }
}
