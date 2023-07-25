package bandlab.waveformeditor.data

import bandlab.waveformeditor.FilesHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WaveformCoordinatesRepository @Inject constructor(
    private val filesHelper: FilesHelper
) {

    suspend fun getCoordinatesFromFile(uri: String): List<Pair<Float, Float>> {
        val coordinates = mutableListOf<Pair<Float, Float>>()
        withContext(Dispatchers.IO) {
            filesHelper.getParsedDataFromFile(uri)!!.forEach { line ->
                line.split(" ").let {
                    coordinates.add(it[0].toFloat() to it[1].toFloat())
                }
            }
        }
        return coordinates
    }

    suspend fun saveCoordinatesToFile(coordinates: List<Pair<Float, Float>>) {
        val lines = coordinates.map {
            "${it.first} ${it.second}"
        }
        withContext(Dispatchers.IO) {
            filesHelper.writeToFile(lines)
        }
    }
}