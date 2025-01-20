package com.mobility.enp.view

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mobility.enp.databinding.ActivityCsvBinding
import com.mobility.enp.viewmodel.UserPassViewModel
import kotlinx.coroutines.launch

class CsvActivity : AppCompatActivity() {
    private val viewModel: UserPassViewModel by viewModels()
    private lateinit var binding: ActivityCsvBinding

    companion object {
        const val TAG = "CSV_ACTIVITY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCsvBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadCSV()
    }

    private fun loadCSV() {
        lifecycleScope.launch {
            val byteArray = viewModel.fetchCsvData()
            byteArray?.let {
                binding.tableView.fromBytes(it).load()
            } ?: run {
                Log.d(TAG, "loadCSV: no data")
            }

        }
    }

}