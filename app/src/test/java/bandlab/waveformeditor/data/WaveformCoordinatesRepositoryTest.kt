package bandlab.waveformeditor.data

import bandlab.waveformeditor.FilesHelper
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class WaveformCoordinatesRepositoryTest{

    private lateinit var waveformCoordinatesRepository: WaveformCoordinatesRepository
    private val filesHelper: FilesHelper = mockk()

    private val testCoordinatesPairs = listOf(
        0.1f to 0.5f,
        0.2f to 0.3f,
    )

    private val testCoordinatesListOfStrings = listOf(
        "0.1 0.5",
        "0.2 0.3",
    )

    @Before
    fun setup() {
        waveformCoordinatesRepository = WaveformCoordinatesRepository(filesHelper)
    }

    @Test
    fun `getCoordinatesFromFile returns list of coordinates`() {
        every { filesHelper.getParsedDataFromFile("") } returns testCoordinatesListOfStrings
        runBlocking {
            val coordinates = waveformCoordinatesRepository.getCoordinatesFromFile("")
            assertEquals(testCoordinatesPairs, coordinates)
        }
    }

    @Test
    fun `saveCoordinatesToFile calls FilesHelper writeToFile with mapped coordinates`() {
        every { filesHelper.writeToFile(testCoordinatesListOfStrings) } returns Unit
        runBlocking {
            waveformCoordinatesRepository.saveCoordinatesToFile(testCoordinatesPairs)
        }
        coVerify { filesHelper.writeToFile(testCoordinatesListOfStrings) }
    }
}