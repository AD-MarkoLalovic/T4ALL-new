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
import com.mobility.enp.databinding.DialogLostTagBinding
import com.mobility.enp.util.setDimensionsPercent

class LostTagDialog : DialogFragment {

    private var _binding: DialogLostTagBinding? = null
    private val binding: DialogLostTagBinding get() = _binding!!

    private lateinit var title: String
    private lateinit var subtitle: String
    private lateinit var onButtonClickInLostTag: OnButtonClickInLostTag

    constructor() : super()

    constructor(
        title: String,
        subtitle: String,
        onButtonClickInLostTag: OnButtonClickInLostTag
    ) : super() {
        this.title = title
        this.subtitle = subtitle
        this.onButtonClickInLostTag = onButtonClickInLostTag
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding = DialogLostTagBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.titleLostTag.text = title
        binding.subtitleLostTag.text = subtitle

        binding.buttonConfirmLostTag.setOnClickListener {

            onButtonClickInLostTag.onClickConfirmed()
            dismiss()
        }

        binding.bttCancelLostTag.setOnClickListener {
            dismiss()
        }

    }

    interface OnButtonClickInLostTag {
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