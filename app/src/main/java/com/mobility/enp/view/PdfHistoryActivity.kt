package com.mobility.enp.view

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.mobility.enp.R
import com.mobility.enp.databinding.ActivityCsvBinding
import com.mobility.enp.databinding.ActivityPdfHistoryBinding
import com.mobility.enp.viewmodel.UserPassViewModel
import kotlinx.coroutines.launch
import kotlin.getValue

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
            } ?: run {
                Log.d(TAG, "loadCSV: no data")
            }

        }
    }
}