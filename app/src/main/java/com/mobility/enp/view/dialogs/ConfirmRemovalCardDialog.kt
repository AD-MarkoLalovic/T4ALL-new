package com.mobility.enp.view.dialogs

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.mobility.enp.R
import com.mobility.enp.databinding.DialogConfirmRemovalCardBinding

class ConfirmRemovalCardDialog(private val listener: ClickedDeleteCardInterface) :
    DialogFragment() {

    private lateinit var binding: DialogConfirmRemovalCardBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.dialog_confirm_removal_card,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bttCancelConfirmRemoval.setOnClickListener {
            dialog?.dismiss()
        }

        binding.bttConfirmRemoval.setOnClickListener {
            listener.onPositiveButtonClicked()
            dialog?.dismiss()
        }
    }

    interface ClickedDeleteCardInterface {
        fun onPositiveButtonClicked()
    }

    override fun onStart() {
        super.onStart()
        setWidthPercent(95)
    }

    private fun DialogFragment.setWidthPercent(percentage: Int) {
        val percent = percentage.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

}