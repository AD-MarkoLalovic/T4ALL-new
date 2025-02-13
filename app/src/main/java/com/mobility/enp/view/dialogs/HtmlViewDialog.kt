package com.mobility.enp.view.dialogs

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.mobility.enp.databinding.PdfDialogBinding
import com.mobility.enp.util.setDimensionsPercent
import com.mobility.enp.viewmodel.HtmlDialogViewModel

class HtmlViewDialog() : DialogFragment() {

    private var _binding: PdfDialogBinding? = null
    private val binding: PdfDialogBinding get() = _binding!!
    private val viewModel: HtmlDialogViewModel by viewModels { HtmlDialogViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding = PdfDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: HtmlViewDialogArgs by navArgs()
        val receivedPair = Pair(args.countryCode, args.documentType)

        setObserver()

        when (receivedPair.first) {
            "ME", "MK" -> {
                viewModel.processContent(
                    receivedPair.first ?: "",
                    receivedPair.second ?: "",
                    requireContext()
                )
            }

            else -> {
                throw IllegalArgumentException("Invalid country code: ${receivedPair.first}")
            }
        }

        binding.confirmButton.setOnClickListener {
            dismiss()
        }
    }

    private fun setObserver() {
        viewModel.filepath.observe(viewLifecycleOwner) { finalPath ->
            binding.webView.settings.javaScriptEnabled = true
            binding.webView.loadUrl("file:///android_asset/" + finalPath)
        }
    }

    override fun onStart() {
        super.onStart()
        setDimensionsPercent(95)
        isCancelable = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}