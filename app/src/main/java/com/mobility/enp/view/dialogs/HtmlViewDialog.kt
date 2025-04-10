package com.mobility.enp.view.dialogs

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.mobility.enp.databinding.PdfDialogBinding
import com.mobility.enp.util.setDimensionsPercent
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.HtmlDialogViewModel
import kotlin.getValue

class HtmlViewDialog() : DialogFragment() {

    private var _binding: PdfDialogBinding? = null
    private val binding: PdfDialogBinding get() = _binding!!
    private val viewModel: HtmlDialogViewModel by viewModels { HtmlDialogViewModel.Factory }
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }

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

        dialog?.setCancelable(false)

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
            setFragmentResult("htmlDialogDismissed", Bundle())
            dismiss()
        }
    }

    private fun setObserver() {
        viewModel.filepath.observe(viewLifecycleOwner) { finalPath ->
            binding.webView.settings.javaScriptEnabled = true
            binding.webView.loadUrl("file:///android_asset/" + finalPath)
        }

        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.confirmButton.backgroundTintList = ColorStateList.valueOf(color)
            }
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