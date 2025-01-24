package com.mobility.enp.view.dialogs

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.mobility.enp.R
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.databinding.DeactivateDialogBinding
import com.mobility.enp.util.Util
import com.mobility.enp.util.setDimensionsPercent
import com.mobility.enp.view.MainActivity
import com.mobility.enp.viewmodel.SupportViewModel

class DeactivateAccountDialog : DialogFragment() {

    private var _binding: DeactivateDialogBinding? = null
    private val binding: DeactivateDialogBinding get() = _binding!!
    private val viewModel: SupportViewModel by viewModels()
    private var errorBody: MutableLiveData<ErrorBody> = MutableLiveData()

    companion object {
        const val TAG = "DEACTIVATE_ACCOUNT_DIALOG"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding = DeactivateDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObserversError()

        binding.bttSendSupportMessage.setOnClickListener {
            val enteredText = binding.enterSupportMessage.text.toString().trim()
            val email = binding.enterEmailAccount.text.toString().trim()
            if (enteredText.isNotEmpty() && email.isNotEmpty()) {
                if (Util.isValidEmail(email)) {
                    binding.progBar.visibility = View.VISIBLE
                    viewModel.sendDeactivationRequest(pair = Pair(email, enteredText), errorBody)
                } else {
                    val toastContext = requireContext()
                    Toast.makeText(
                        toastContext,
                        getString(R.string.email_not_valid),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                val toastContext = requireContext()
                Toast.makeText(
                    toastContext,
                    getString(R.string.please_enter_all_required_data),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.deactivateAccountDialogClose.setOnClickListener {
            dismiss()
        }
    }


    private fun openSuccessDialog() {
        val generalDialog =
            GeneralMessageDialog(requireContext().getString(R.string.support_successful_mail),
                requireContext().getString(R.string.support_successful_massage))
        generalDialog.show(childFragmentManager, "GeneralDialogSupport")
    }

    private fun setObserversError() {
        errorBody = MutableLiveData()
        errorBody.observe(viewLifecycleOwner) { errorBody ->
            binding.progBar.visibility = View.GONE
            context?.let { context ->
                Toast.makeText(
                    context, errorBody.errorBody, Toast.LENGTH_SHORT
                ).show()
                if (errorBody.errorCode == 405 || errorBody.errorCode == 401) {
                    MainActivity.logoutOnInvalidToken(context, findNavController())
                }
            }
        }

        viewModel.deactivateAccount.observe(viewLifecycleOwner) { response ->
            response.let {
                binding.progBar.visibility = View.GONE
                Log.d(TAG, "deactivation response: $response")
                dialog?.dismiss()
                openSuccessDialog()
            }
        }

        viewModel.checkNetSendSupport.observe(viewLifecycleOwner) { hasInternet ->
            binding.progBar.visibility = View.GONE
            if (hasInternet != null && !hasInternet) {

                val bundle = Bundle().apply {
                    putString(getString(R.string.title), getString(R.string.no_connection_title))
                    putString(
                        getString(R.string.subtitle),
                        getString(R.string.please_connect_to_the_internet)
                    )
                }

                findNavController().navigate(R.id.action_global_noInternetConnectionDialog, bundle)

                val binding = (activity as MainActivity).binding
                MainActivity.showSnackMessage(getString(R.string.checking_for_connection), binding)

            }
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