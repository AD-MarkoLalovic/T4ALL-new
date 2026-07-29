package com.mobility.enp.view.dialogs

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mobility.enp.R
import com.mobility.enp.databinding.DialogSupportBinding
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.util.setDimensionsPercent
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.fragments.my_profile.ProfileFragment.Companion.TAG
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.SupportViewModel

class SupportDialog : DialogFragment() {

    private var _binding: DialogSupportBinding? = null
    private val binding: DialogSupportBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val viewModel: SupportViewModel by viewModels { SupportViewModel.Factory }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = true

        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSupportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObserversError()
        setFranchiser()

        binding.bttSendSupportMessage.setOnClickListener {
            val enteredText = binding.enterSupportMessage.text.toString()
            if (enteredText.isNotEmpty()) {
                viewModel.sendSupportMessage(enteredText)
            } else {
                val toastContext = requireContext()
                Toast.makeText(
                    toastContext,
                    getString(R.string.enter_support_message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        observeSupportMessageSentStatus()

        binding.supportDialogClose.setOnClickListener {
            dismiss()
        }
    }

    private fun setFranchiser() {
        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.let { data ->
                binding.bttSendSupportMessage.backgroundTintList =
                    ColorStateList.valueOf(data.franchisePrimaryColor)
                binding.enterSupportMessage.setTextColor(ColorStateList.valueOf(data.franchisePrimaryColor))

                binding.supportDialogClose.setImageResource(data.franchiseCloseButton)

                binding.supportDialogInput.apply {
                    boxStrokeColor = data.franchisePrimaryColor

                    val editText = this.editText
                    editText?.textSelectHandle?.setTint(data.franchisePrimaryColor)
                    editText?.setTextColor(data.franchisePrimaryColor)

                    val states = arrayOf(
                        intArrayOf(android.R.attr.state_pressed),  // pressed
                        intArrayOf(android.R.attr.state_focused),  // focused
                        intArrayOf()                               // default
                    )

                    val colors = intArrayOf(
                        data.franchisePrimaryColor,        // pressed
                        data.franchisePrimaryColor,        // focused
                        data.franchisePrimaryColor         // default
                    )

                    cursorColor = ColorStateList(states, colors)
                }
            }
        }
    }

    private fun observeSupportMessageSentStatus() {
        collectLatestLifecycleFlow(viewModel.supportMessageSentLiveData) { flow ->
            when (flow) {
                is SubmitResult.Success -> {
                    openSuccessfulMailDialog()
                    dismiss()
                }

                is SubmitResult.FailureServerError -> {
                    logMessage(getString(R.string.server_error_msg))
                }

                is SubmitResult.FailureApiError -> {
                    logMessage(flow.errorMessage)
                }

                is SubmitResult.InvalidApiToken -> {
                    logMessage(flow.errorMessage)
                    MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
                }

                else -> {
                    SubmitResult.Empty
                }
            }
        }
    }

    private fun openSuccessfulMailDialog() {
        GeneralMessageDialog.newInstance(
            requireContext().getString(R.string.support_successful_mail),
            requireContext().getString(R.string.support_successful_massage)
        ).show(parentFragmentManager, "SupportDialog")
    }

    private fun setObserversError() {
        viewModel.checkNetSendSupport.observe(viewLifecycleOwner) { hasInternet ->
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

    private fun logMessage(message: String) {
        Log.d(TAG, "sendSupportMsg: $message")
    }

    override fun onStart() {
        super.onStart()
        val isLandscape = resources.configuration.orientation ==
                android.content.res.Configuration.ORIENTATION_LANDSCAPE

        setDimensionsPercent(if (isLandscape) 85 else 95)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}