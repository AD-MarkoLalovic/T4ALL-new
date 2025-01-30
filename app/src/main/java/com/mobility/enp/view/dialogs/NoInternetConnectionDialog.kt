package com.mobility.enp.view.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.mobility.enp.R
import com.mobility.enp.databinding.GeneralDialogBinding
import com.mobility.enp.util.setDimensionsPercent

class NoInternetConnectionDialog : DialogFragment() {

    private var _binding: GeneralDialogBinding? = null
    private val binding: GeneralDialogBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding = GeneralDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.title.text = arguments?.getString(getString(R.string.title)) ?: ""
        binding.subTitle.text = arguments?.getString(getString(R.string.subtitle)) ?: ""

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