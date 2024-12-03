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
        setWidthPercent(95)
        isCancelable = false
    }

    private fun DialogFragment.setWidthPercent(percentage: Int) {
        val percent = percentage.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}