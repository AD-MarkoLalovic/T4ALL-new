package com.mobility.enp.view.dialogs

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.mobility.enp.databinding.PdfDialogBinding
import com.mobility.enp.util.AssetHelper
import com.mobility.enp.util.setDimensionsPercent

class HtmlViewDialog() : DialogFragment() {

    private var _binding: PdfDialogBinding? = null
    private val binding: PdfDialogBinding get() = _binding!!

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

        val list = AssetHelper.getFileNames(requireContext(),args.documentType ?: "")
        Log.d("PDF_DIA", "list: ${list.toString()}")

        binding.webView.settings.javaScriptEnabled = true
        binding.webView.loadUrl("file:///android_asset/"+list[0])

        binding.confirmButton.setOnClickListener {
            dismiss()
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