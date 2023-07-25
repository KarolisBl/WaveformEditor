package bandlab.waveformeditor.ui

import app.cash.turbine.test
import bandlab.waveformeditor.MainDispatcherRule
import bandlab.waveformeditor.R
import bandlab.waveformeditor.data.WaveformCoordinatesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WaveformViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: WaveformViewModel
    private val waveformCoordinatesRepository: WaveformCoordinatesRepository = mockk()

    private val testCoordinates = listOf(
        0.1f to 0.5f,
        0.2f to 0.3f,
        0.4f to 0.6f,
        0.5f to 0.7f
    )

    @Before
    fun setup() {
        viewModel = WaveformViewModel(waveformCoordinatesRepository)
    }

    @Test
    fun `emits NotImported state on init`() {
        viewModel = WaveformViewModel(waveformCoordinatesRepository)
        runBlocking {
            viewModel.state.test {
                assertEquals(
                    WaveformState.NotImported,
                    awaitItem()
                )
            }
        }
    }

    @Test
    fun `onFileToImportSelected emits Success state on success`() {
        coEvery { waveformCoordinatesRepository.getCoordinatesFromFile("") } returns testCoordinates

        viewModel.onFileToImportSelected("")

        runBlocking {
            viewModel.state.test {
                assertEquals(
                    WaveformState.Success(testCoordinates, 0, 3),
                    awaitItem()
                )
            }
        }
    }

    @Test
    fun `onFileToImportSelected emits notifyUserEvent with correct message on error`() {
        coEvery { waveformCoordinatesRepository.getCoordinatesFromFile("") } throws RuntimeException()

        runBlocking {
            viewModel.notifyUserEvent.test {
                viewModel.onFileToImportSelected("")
                assertEquals(
                    R.string.waveform_import_error,
                    awaitItem()
                )
            }
        }
    }

    @Test
    fun `onExportWaveformClicked emits notifyUserEvent with correct message on success`() {
        coEvery { waveformCoordinatesRepository.getCoordinatesFromFile("") } returns testCoordinates
        viewModel.onFileToImportSelected("")
        coEvery { waveformCoordinatesRepository.saveCoordinatesToFile(any()) } returns Unit

        runBlocking {
            viewModel.notifyUserEvent.test {
                viewModel.onExportWaveformClicked()
                assertEquals(
                    R.string.waveform_export_success,
                    awaitItem()
                )
            }
        }
    }

    @Test
    fun `onExportWaveformClicked emits notifyUserEvent with correct message on error`() {
        coEvery { waveformCoordinatesRepository.saveCoordinatesToFile(any()) } throws RuntimeException()

        runBlocking {
            viewModel.notifyUserEvent.test {
                viewModel.onExportWaveformClicked()
                assertEquals(
                    R.string.waveform_export_error,
                    awaitItem()
                )
            }
        }
    }

    @Test
    fun `onExportWaveformClicked calls WaveformCoordinatesRepository saveCoordinatesToFile with subListed coordinates`() {
        val subListedCoordinates = listOf(
            0.2f to 0.3f,
            0.4f to 0.6f,
        )
        coEvery { waveformCoordinatesRepository.getCoordinatesFromFile("") } returns testCoordinates
        viewModel.onFileToImportSelected("")
        viewModel.onSelectionChanged(1, 2)
        coEvery { waveformCoordinatesRepository.saveCoordinatesToFile(subListedCoordinates) } returns Unit

        viewModel.onExportWaveformClicked()
        coVerify { waveformCoordinatesRepository.saveCoordinatesToFile(subListedCoordinates) }
    }

    @Test
    fun `onSelectionChanged emits Success state with changed selection`() {
        coEvery { waveformCoordinatesRepository.getCoordinatesFromFile("") } returns testCoordinates
        viewModel.onFileToImportSelected("")
        viewModel.onSelectionChanged(1, 2)
        runBlocking {
            viewModel.state.test {
                assertEquals(
                    WaveformState.Success(testCoordinates, 1, 2),
                    awaitItem()
                )
            }
        }
    }
}