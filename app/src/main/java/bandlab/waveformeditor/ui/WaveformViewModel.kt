package bandlab.waveformeditor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bandlab.waveformeditor.R
import bandlab.waveformeditor.data.WaveformCoordinatesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WaveformViewModel @Inject constructor(
    private val waveformCoordinatesRepository: WaveformCoordinatesRepository
) : ViewModel() {

    private val _state = MutableStateFlow<WaveformState>(WaveformState.NotImported)
    val state get() = _state.asStateFlow()

    private val _notifyUserEvent = MutableSharedFlow<NotifyUserMessageResId>()
    val notifyUserEvent get() = _notifyUserEvent.asSharedFlow()

    fun onFileToImportSelected(uri: String) {
        viewModelScope.launch {
            runCatching {
                val coordinates = waveformCoordinatesRepository.getCoordinatesFromFile(uri)
                _state.emit(
                    WaveformState.Success(
                        coordinates = coordinates,
                        startSelection = 0,
                        endSelection = coordinates.size - 1
                    )
                )
            }.onFailure {
                _notifyUserEvent.emit(R.string.waveform_import_error)
            }

        }
    }

    fun onExportWaveformClicked() {
        viewModelScope.launch {
            runCatching {
                val state = (_state.value as WaveformState.Success)
                val coordinatesToSave = state.coordinates
                    .subList(state.startSelection, state.endSelection + 1)
                waveformCoordinatesRepository.saveCoordinatesToFile(coordinatesToSave)
                _notifyUserEvent.emit(R.string.waveform_export_success)
            }.onFailure {
                _notifyUserEvent.emit(R.string.waveform_export_error)
            }
        }
    }

    fun onSelectionChanged(startSelection: Int, endSelection: Int) {
        viewModelScope.launch {
            (_state.value as? WaveformState.Success)?.let { currentState ->
                _state.emit(
                    WaveformState.Success(
                        coordinates = currentState.coordinates,
                        startSelection = startSelection,
                        endSelection = endSelection
                    )
                )
            }
        }
    }
}

typealias NotifyUserMessageResId = Int