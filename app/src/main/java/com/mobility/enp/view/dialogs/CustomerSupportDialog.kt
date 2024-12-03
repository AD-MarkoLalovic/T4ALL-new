package com.mobility.enp.view.dialogs

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.mobility.enp.R
import com.mobility.enp.databinding.ContactFormDialogBinding
import com.mobility.enp.util.SubmitResultCustomerSupport
import com.mobility.enp.viewmodel.CustomerSupportViewModel

class CustomerSupportDialog : DialogFragment() {

    private var _binding: ContactFormDialogBinding? = null
    private val binding: ContactFormDialogBinding get() = _binding!!
    private val viewModel: CustomerSupportViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding = ContactFormDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        setWidthPercent(95)
        isCancelable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.submitSuccessful.observe(viewLifecycleOwner) { result ->
            when (result) {
                is SubmitResultCustomerSupport.Loading -> {
                    binding.loadingCustomerSupport.visibility = View.VISIBLE
                }

                is SubmitResultCustomerSupport.Success -> {
                    binding.loadingCustomerSupport.visibility = View.GONE

                    showDialog(
                        getString(R.string.support_successful_mail),
                        getString(R.string.support_successful_massage)
                    )
                    dismiss()
                }

                is SubmitResultCustomerSupport.Failure -> {
                    binding.loadingCustomerSupport.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.message_was_not_send),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is SubmitResultCustomerSupport.NoNetwork -> {
                    binding.loadingCustomerSupport.visibility = View.GONE

                    showDialog(
                        getString(R.string.no_connection_title),
                        getString(R.string.please_connect_to_the_internet)
                    )
                }
            }

        }

        binding.contactFormSendButton.setOnClickListener {
            val subject = binding.contactFormInputEditReason.text.toString()
            val email = binding.contactFormInputEditEmail.text.toString()
            val message = binding.contactFormInputEditMessages.text.toString()

            if (subject.isNotEmpty() && email.isNotEmpty() && message.isNotEmpty()) {
                if (isValidEmail(email)) {
                    viewModel.submitCustomerSupport(subject, email, message)
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

    private fun DialogFragment.setWidthPercent(percentage: Int) {
        val percent = percentage.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showDialog(title: String, subtitle: String) {
        val dialog = LoginNoInternetConnectionDialog()
        val bundle = Bundle().apply {
            putString(getString(R.string.title), title)
            putString(getString(R.string.subtitle), subtitle)
        }
        dialog.arguments = bundle
        dialog.show(parentFragmentManager, "CustomerSupport")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}