package bandlab.waveformeditor

import android.content.Context
import android.net.Uri
import android.os.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class FilesHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getParsedDataFromFile(uri: String): List<String>? {
        val inputStream = context.contentResolver.openInputStream(Uri.parse(uri))
        val lines = inputStream?.bufferedReader()?.readLines()
        inputStream?.close()
        return lines
    }

    fun writeToFile(lines: List<String>) {
        val directory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(directory, "slice-${System.currentTimeMillis()}.txt")
        val fileOutputStream = FileOutputStream(file)
        fileOutputStream.bufferedWriter()
            .apply {
                lines.forEach { appendLine(it) }
                close()
            }
    }
}