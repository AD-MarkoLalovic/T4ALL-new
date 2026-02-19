package com.mobility.enp.view.dialogs

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.mobility.enp.R
import com.mobility.enp.databinding.ContactFormDialogBinding
import com.mobility.enp.util.setDimensionsPercent
import com.mobility.enp.viewmodel.CustomerSupportViewModel
import androidx.core.graphics.drawable.toDrawable
import com.mobility.enp.data.model.login.CustomerSupport
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResultFold
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.util.toast

class CustomerSupportDialog : DialogFragment() {

    private var _binding: ContactFormDialogBinding? = null
    private val binding: ContactFormDialogBinding get() = _binding!!
    private val viewModel: CustomerSupportViewModel by viewModels { CustomerSupportViewModel.factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ContactFormDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = true

        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setCanceledOnTouchOutside(false)
        }

    }

    override fun onStart() {
        super.onStart()
        val isLandscape = resources.configuration.orientation ==
                android.content.res.Configuration.ORIENTATION_LANDSCAPE

        setDimensionsPercent(if (isLandscape) 85 else 95)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        collectLatestLifecycleFlow(viewModel.submitSuccessful) { resultFold ->
            when (resultFold) {
                is SubmitResultFold.Failure -> {
                    binding.loadingCustomerSupport.visibility = View.GONE
                    handleError(resultFold.error)

                    viewModel.resetSubmitState()
                }

                SubmitResultFold.Idle -> {}
                SubmitResultFold.Loading -> binding.loadingCustomerSupport.visibility = View.VISIBLE
                is SubmitResultFold.Success<*> -> {
                    binding.loadingCustomerSupport.visibility = View.GONE

                    dialog(
                        title = getString(R.string.support_successful_mail),
                        subtitle = getString(R.string.support_successful_massage)
                    )
                    dismiss()
                }
            }
        }

        binding.contactFormSendButton.setOnClickListener {
            val subject = binding.contactFormInputEditReason.text.toString()
            val email = binding.contactFormInputEditEmail.text.toString()
            val message = binding.contactFormInputEditMessages.text.toString()

            if (subject.isNotEmpty() && email.isNotEmpty() && message.isNotEmpty()) {
                if (isValidEmail(email)) {
                    viewModel.sendCustomerSupport(
                        CustomerSupport(
                            subject = subject,
                            email = email,
                            message = message
                        )
                    )
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.enter_valid_email_address),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.fill_in_all_fields),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.contactFormClose.setOnClickListener {
            dismiss()
        }

    }

    private fun handleError(error: Throwable) {
        when (error) {
            is NetworkError.ServerError -> {
                toast(getString(R.string.server_error_msg))
            }

            is NetworkError.ApiError -> {
                toast(
                    error.errorResponse.message ?: getString(R.string.server_error_msg)
                )
            }

            is NetworkError.NoConnection -> {
                dialog(
                    title = getString(R.string.no_connection_title),
                    subtitle = getString(R.string.please_connect_to_the_internet)
                )
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun dialog(title: String, subtitle: String) {
        GeneralMessageDialog.newInstance(
            title = title,
            subtitle = subtitle
        ).show(parentFragmentManager, "CustomerSupport")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}