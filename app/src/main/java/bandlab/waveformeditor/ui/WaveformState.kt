package bandlab.waveformeditor.ui

sealed class WaveformState {
    object NotImported : WaveformState()
    data class Success(
        val coordinates: List<Pair<Float, Float>>,
        val startSelection: Int,
        val endSelection: Int,
    ) : WaveformState()
}