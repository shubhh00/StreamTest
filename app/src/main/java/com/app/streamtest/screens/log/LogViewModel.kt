package com.app.streamtest.screens.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.streamtest.domain.device.DeviceInfoRepository
import com.app.streamtest.domain.results.ResultsRepository
import com.app.streamtest.domain.results.TestResult
import com.app.streamtest.domain.results.groupByFilm
import com.app.streamtest.domain.stats.QualityLineModel
import com.app.streamtest.domain.stats.ResultsSummary
import com.app.streamtest.domain.stats.buildQualityLine
import com.app.streamtest.domain.stats.summarize
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** One table row. The film title is only shown on the first row of each film. */
data class LogRow(
    val result: TestResult,
    val showFilmTitle: Boolean,
    val qualityLine: QualityLineModel,
)

data class LogState(
    val deviceName: String = "",
    val testCount: Int = 0,
    val summary: ResultsSummary? = null,
    val rows: List<LogRow> = emptyList(),
)

@HiltViewModel
class LogViewModel @Inject constructor(
    private val resultsRepository: ResultsRepository,
    private val deviceInfoRepository: DeviceInfoRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LogState())
    val state: StateFlow<LogState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val name = deviceInfoRepository.getCapabilities().deviceName
            _state.update { it.copy(deviceName = name) }
        }
        viewModelScope.launch {
            resultsRepository.observeAll().collect { results ->
                _state.update {
                    it.copy(testCount = results.size, summary = summarize(results), rows = toRows(results))
                }
            }
        }
    }

    private fun toRows(results: List<TestResult>): List<LogRow> =
        groupByFilm(results).flatMap { filmResults ->
            filmResults.mapIndexed { index, result ->
                LogRow(
                    result = result,
                    showFilmTitle = index == 0,
                    qualityLine = buildQualityLine(
                        changes = result.timeline.qualityChanges,
                        freezes = result.timeline.freezes,
                        playedUntilMs = result.timeline.playedUntilMs,
                        durationMs = result.timeline.lengthMs,
                    ),
                )
            }
        }
}
