package com.mobility.enp.view.dialogs

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mobility.enp.R
import com.mobility.enp.databinding.DialogForgotPassBinding
import com.mobility.enp.util.setDimensionsPercent
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible

class ForgotPassDialog : DialogFragment() {

    private val args: ForgotPassDialogArgs by navArgs()

    private var _binding: DialogForgotPassBinding? = null
    private val binding: DialogForgotPassBinding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogForgotPassBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isSuccess = args.sentSuccessfully
        val message = args.message ?: getString(R.string.subtitle_dialog_forgot_pass)
        render(isSuccess, message)

        binding.bttDialogForgotPass.setOnClickListener {
            dismissAllowingStateLoss()
            if (isSuccess) {
                runCatching {
                    findNavController()
                        .navigate(R.id.action_global_loginFragment)
                }
            }
        }
    }

    private fun render(isSuccess: Boolean, message: String) = with(binding) {
        errorDialogForgotPass.isVisible = !isSuccess
        subtitleDialogForgotPass.text =
            if (isSuccess) getString(R.string.subtitle_forgot_pass) else message
        bttDialogForgotPass.text =
            if (isSuccess) getString(R.string.confirm) else getString(R.string.button_try_again)
    }

    override fun onStart() {
        super.onStart()
        setDimensionsPercent(95)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}