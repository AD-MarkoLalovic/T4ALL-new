package com.mobility.enp.view

import android.content.ContentValues
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mobility.enp.databinding.ActivityPdfHistoryBinding
import com.mobility.enp.viewmodel.UserPassViewModel
import kotlinx.coroutines.launch

class PdfHistoryActivity : AppCompatActivity() {
    private val vModel: UserPassViewModel by viewModels { UserPassViewModel.Factory }
    private lateinit var binding: ActivityPdfHistoryBinding

    companion object {
        const val TAG = "CSV_ACTIVITY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPdfHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadCSV()
    }

    private fun loadCSV() {
        lifecycleScope.launch {
            val byteArray = vModel.fetchPDFData()
            byteArray?.let {
                binding.tableView.fromBytes(it).load()
                savePdfToDownloads(byteArray, "history_export_pdf_${System.currentTimeMillis()}")
            } ?: run {
                Log.d(TAG, "loadCSV: no data")
            }
        }
    }

    fun savePdfToDownloads(bytes: ByteArray, fileName: String) {
        val resolver = contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.pdf")
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

        uri?.let {
            resolver.openOutputStream(it)?.use { output ->
                output.write(bytes)
            }
        }
    }
}