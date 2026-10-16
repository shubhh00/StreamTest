package com.app.streamtest.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.streamtest.domain.device.DeviceDetails
import com.app.streamtest.domain.device.DeviceInfoRepository
import com.app.streamtest.domain.results.ResultsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The sections in the left column of Settings. */
enum class SettingsSection(val title: String) {
    THIS_TV("This TV"),
    DECODERS("Video decoders"),
    DISPLAY("Display"),
    RESULTS("Saved results"),
    ABOUT("About & credits"),
}

/** Clearing results takes two presses of OK, so it can't happen by accident. */
enum class ClearStep { IDLE, CONFIRM, DONE }

/** All decoders for one video format, e.g. every "video/hevc" decoder. */
data class DecoderGroup(
    val mimeType: String,
    val decoderNames: List<String>,
    /** How many are hardware chips; null if the device doesn't say (Android 9 and older). */
    val hardwareCount: Int?,
)

data class SettingsState(
    val section: SettingsSection = SettingsSection.THIS_TV,
    val details: DeviceDetails? = null,
    val decoderGroups: List<DecoderGroup> = emptyList(),
    val savedCount: Int = 0,
    val clearStep: ClearStep = ClearStep.IDLE,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val deviceInfoRepository: DeviceInfoRepository,
    private val resultsRepository: ResultsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val details = deviceInfoRepository.getDetails()
            _state.update { it.copy(details = details, decoderGroups = groupDecoders(details)) }
        }
        viewModelScope.launch {
            resultsRepository.observeAll().collect { results ->
                _state.update { it.copy(savedCount = results.size) }
            }
        }
    }

    fun onSectionFocused(section: SettingsSection) {
        _state.update {
            // Moving away from "Saved results" cancels a half-done clear.
            val step = if (it.clearStep == ClearStep.CONFIRM) ClearStep.IDLE else it.clearStep
            it.copy(section = section, clearStep = step)
        }
    }

    /** First press asks to confirm; the second press deletes everything. */
    fun onClearPressed() {
        when (_state.value.clearStep) {
            ClearStep.IDLE -> if (_state.value.savedCount > 0) {
                _state.update { it.copy(clearStep = ClearStep.CONFIRM) }
            }
            ClearStep.CONFIRM -> viewModelScope.launch {
                resultsRepository.deleteAll()
                _state.update { it.copy(clearStep = ClearStep.DONE) }
            }
            ClearStep.DONE -> Unit
        }
    }

    private fun groupDecoders(details: DeviceDetails): List<DecoderGroup> =
        details.videoDecoders
            .groupBy { it.mimeType }
            .map { (mimeType, decoders) ->
                val hardwareKnown = decoders.all { it.isHardware != null }
                DecoderGroup(
                    mimeType = mimeType,
                    decoderNames = decoders.map { it.name }.distinct(),
                    hardwareCount = if (hardwareKnown) decoders.count { it.isHardware == true } else null,
                )
            }
            .sortedBy { it.mimeType }
}
