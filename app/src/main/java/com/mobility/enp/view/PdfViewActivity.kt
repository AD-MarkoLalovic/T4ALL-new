package com.mobility.enp.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.mobility.enp.R
import com.mobility.enp.databinding.ActivityPdfViewBinding
import com.mobility.enp.util.FragmentResultKeys
import com.mobility.enp.view.dialogs.GeneralMessageDialog
import com.mobility.enp.viewmodel.MyInvoicesViewModel
import kotlinx.coroutines.launch

class PdfViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewBinding
    private val viewModel: MyInvoicesViewModel by viewModels { MyInvoicesViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.activity_pdf_view, null, false)

        enableEdgeToEdge()
        setContentView(binding.root)

        supportFragmentManager.setFragmentResultListener(
            FragmentResultKeys.GENERAL_DIALOG_RESULT,
            this
        ) { _, bundle ->
            val confirmed = bundle.getBoolean(FragmentResultKeys.GENERAL_DIALOG_CONFIRMED, false)
            if (confirmed) {
                finish()
            }
        }

        loadPdf()

        lifecycleScope.launch {
            viewModel.loadPdf()
        }
    }

    private fun loadPdf() {
        viewModel.pdfData.observe(this) { byteArray ->
            binding.pdfView.fromBytes(byteArray).load()
        }
        viewModel.openDialogForNoPdfData.observe(this) { openDialog ->
            if (openDialog) {
                GeneralMessageDialog.newInstance(
                    title = getString(R.string.pdf_dialog_title_error),
                    subtitle = getString(R.string.pdf_dialog_subtitle_error),
                    resultKey = FragmentResultKeys.GENERAL_DIALOG_RESULT,
                    resultValueKey = FragmentResultKeys.GENERAL_DIALOG_CONFIRMED
                ).show(supportFragmentManager, "PdfDialog")
            }
        }
    }
}