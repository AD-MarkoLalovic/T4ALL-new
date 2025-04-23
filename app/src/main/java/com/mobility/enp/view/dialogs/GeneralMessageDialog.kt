package com.mobility.enp.view.dialogs

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.mobility.enp.databinding.GeneralDialogBinding
import com.mobility.enp.util.setDimensionsPercent
import androidx.core.graphics.drawable.toDrawable

class GeneralMessageDialog(
    private val title: String,
    private val subtitle: String
) : DialogFragment() {

    private var _binding: GeneralDialogBinding? = null
    private val binding: GeneralDialogBinding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        _binding = GeneralDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.title.text = title
        binding.subTitle.text = subtitle
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