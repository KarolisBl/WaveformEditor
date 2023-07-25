package bandlab.waveformeditor.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import bandlab.waveformeditor.databinding.WaveformActivityBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WaveformActivity : AppCompatActivity() {

    private val viewModel: WaveformViewModel by viewModels()

    private lateinit var binding: WaveformActivityBinding

    private val filePickerActivityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                activityResult.data?.data?.let { uri ->
                    viewModel.onFileToImportSelected(uri.toString())
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WaveformActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        observeState()
        observeNotifyUserEvents()
    }

    private fun initViews() {
        initWaveformView()
        initImportButton()
        initExportButton()
    }

    private fun initWaveformView() {
        binding.waveformView.setOnSelectionChangedListener { startSelection, endSelection ->
            viewModel.onSelectionChanged(startSelection, endSelection)
        }
    }

    private fun initImportButton() {
        binding.importWaveformButton.setOnClickListener {
            launchFilePicker()
        }
    }

    private fun launchFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
        }
        filePickerActivityResultLauncher.launch(intent)
    }

    private fun initExportButton() {
        binding.exportWaveformButton.setOnClickListener {
            viewModel.onExportWaveformClicked()
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    WaveformState.NotImported -> handleNotImportedState()
                    is WaveformState.Success -> handleSuccessState(state)
                }
            }
        }
    }

    private fun handleNotImportedState() {
        binding.apply {
            waveformView.visibility = View.INVISIBLE
            waveformNotImportedGroup.visibility = View.VISIBLE
            exportWaveformButton.isEnabled = false
        }
    }

    private fun handleSuccessState(state: WaveformState.Success) {
        binding.apply {
            waveformView.visibility = View.VISIBLE
            waveformNotImportedGroup.visibility = View.GONE
            exportWaveformButton.isEnabled = true
            waveformView.updateData(
                state.coordinates,
                state.startSelection,
                state.endSelection
            )
        }
    }

    private fun observeNotifyUserEvents() {
        lifecycleScope.launch {
            viewModel.notifyUserEvent.collect { errorTextResId ->
                Toast.makeText(this@WaveformActivity, errorTextResId, Toast.LENGTH_LONG).show()
            }
        }
    }
}