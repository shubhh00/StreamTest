package com.app.streamtest.screens.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.app.streamtest.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// @HiltViewModel + @Inject constructor: Hilt builds this ViewModel and passes in
// what it needs. The ExoPlayer will be created here in Milestone 3.
@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Navigation copies route arguments (the {streamId} in "player/{streamId}")
    // into SavedStateHandle, so the ViewModel can read which stream to play.
    val streamId: String = checkNotNull(savedStateHandle[Routes.STREAM_ID_ARG])
}
