package com.mobility.enp.view.dialogs

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.mobility.enp.R
import com.mobility.enp.databinding.DialogForgotPassBinding

class ForgotPassDialog : DialogFragment() {

    private var _binding: DialogForgotPassBinding? = null
    private val binding: DialogForgotPassBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding = DialogForgotPassBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val retryMode = arguments?.getBoolean(getString(R.string.bool)) == true

        if (retryMode) {
            binding.errorDialogForgotPass.visibility = View.GONE
            binding.subtitleDialogForgotPass.text = getString(R.string.subtitle_forgot_pass)
            binding.bttDialogForgotPass.text = getString(R.string.confirm)
        } else {
            binding.errorDialogForgotPass.visibility = View.VISIBLE
            binding.subtitleDialogForgotPass.text = getString(R.string.subtitle_dialog_forgot_pass)
            binding.bttDialogForgotPass.text = getString(R.string.button_try_again)
        }

        binding.bttDialogForgotPass.setOnClickListener {
            if (retryMode) {
                dialog?.dismiss()
                findNavController().navigate(R.id.action_global_loginFragment)
            } else {
                dialog?.dismiss()
            }
        }

    }

    override fun onStart() {
        super.onStart()
        if (isTablet(requireContext())) {
            setWidthPercent(80)
        } else {
            setWidthPercent(95)
        }
        isCancelable = false
    }

    private fun DialogFragment.setWidthPercent(percentage: Int) {
        val percent = percentage.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun isTablet(context: Context): Boolean {
        val configuration: Configuration = context.resources.configuration
        val smallestScreenWidthDp = configuration.smallestScreenWidthDp
        return smallestScreenWidthDp >= 600
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}