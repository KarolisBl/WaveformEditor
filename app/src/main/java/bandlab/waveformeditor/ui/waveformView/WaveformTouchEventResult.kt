package bandlab.waveformeditor.ui.waveformView

sealed class WaveformTouchEventResult {
    data class StartSelectionChanged(val selection: Int) : WaveformTouchEventResult()
    data class EndSelectionChanged(val selection: Int) : WaveformTouchEventResult()
    object SelectionNotChanged : WaveformTouchEventResult()
}