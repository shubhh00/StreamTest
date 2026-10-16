package com.app.streamtest.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.streamtest.domain.catalog.CatalogRepository
import com.app.streamtest.domain.catalog.ExtraStream
import com.app.streamtest.domain.catalog.Film
import com.app.streamtest.domain.device.DeviceInfoRepository
import com.app.streamtest.domain.device.DeviceSummary
import com.app.streamtest.domain.device.toSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Which item in the left column is selected; the right column shows its details. */
sealed interface LibrarySelection {
    data class FilmAt(val index: Int) : LibrarySelection
    data class ExtraAt(val index: Int) : LibrarySelection
}

data class LibraryState(
    val isLoading: Boolean = true,
    val films: List<Film> = emptyList(),
    val extras: List<ExtraStream> = emptyList(),
    val selection: LibrarySelection = LibrarySelection.FilmAt(0),
    /** null until the device check has finished. */
    val deviceSummary: DeviceSummary? = null,
    /** The row the user last opened, so focus can go back to it after the Player. */
    val lastOpenedStreamId: String? = null,
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository,
    private val deviceInfoRepository: DeviceInfoRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    init {
        // Two separate jobs, so the film list shows without waiting for the device check.
        viewModelScope.launch {
            val catalog = catalogRepository.getCatalog()
            _state.update { it.copy(isLoading = false, films = catalog.films, extras = catalog.extras) }
        }
        viewModelScope.launch {
            val summary = deviceInfoRepository.getCapabilities().toSummary()
            _state.update { it.copy(deviceSummary = summary) }
        }
    }

    /** Called when D-pad focus lands on a film: the right column switches to its versions. */
    fun onFilmFocused(index: Int) {
        _state.update { it.copy(selection = LibrarySelection.FilmAt(index)) }
    }

    /**
     * Called just before opening the Player. This ViewModel stays alive while the Player
     * is on top (the Library is still on the back stack), so the id is still here on return.
     */
    fun onStreamOpened(streamId: String) {
        _state.update { it.copy(lastOpenedStreamId = streamId) }
    }

    /** Called when D-pad focus lands on a "More streams" row. */
    fun onExtraFocused(index: Int) {
        _state.update { it.copy(selection = LibrarySelection.ExtraAt(index)) }
    }
}
