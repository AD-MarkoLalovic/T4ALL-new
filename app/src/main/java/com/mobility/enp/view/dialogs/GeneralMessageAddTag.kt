package com.mobility.enp.view.dialogs

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.mobility.enp.databinding.DialogConfirmedAddTagBinding
import com.mobility.enp.util.setDimensionsPercent

class GeneralMessageAddTag : DialogFragment {

    private var _binding: DialogConfirmedAddTagBinding? = null
    private val binding: DialogConfirmedAddTagBinding get() = _binding!!
    private lateinit var onButtonClick: OnButtonClick

    constructor() : super()

    constructor(onButtonClick: OnButtonClick) : super() {
        this.onButtonClick = onButtonClick
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding = DialogConfirmedAddTagBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.confirmAddTag.setOnClickListener {
            dialog?.dismiss()
            onButtonClick.onClickConfirmed()
        }

        binding.confirmedAddTagDialogClose.setOnClickListener {
            dismiss()
        }
    }

    interface OnButtonClick {
        fun onClickConfirmed()
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