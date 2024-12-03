package com.mobility.enp.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.mobility.enp.R
import com.mobility.enp.databinding.ActivityPdfViewBinding
import com.mobility.enp.viewmodel.MyInvoicesViewModel
import kotlinx.coroutines.launch

class PdfViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewBinding
    private val viewModel: MyInvoicesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.activity_pdf_view, null, false)

        enableEdgeToEdge()
        setContentView(binding.root)

        loadPdf()

        lifecycleScope.launch {
            viewModel.loadPdf()
        }
    }

    private fun loadPdf() {
        viewModel.pdfData.observe(this) { byteArray ->
            binding.pdfView.fromBytes(byteArray).load()
        }
    }
}