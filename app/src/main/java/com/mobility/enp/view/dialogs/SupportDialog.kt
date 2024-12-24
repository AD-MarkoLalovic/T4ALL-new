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
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.mobility.enp.R
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.databinding.DialogSupportBinding
import com.mobility.enp.view.MainActivity
import com.mobility.enp.viewmodel.SupportViewModel

class SupportDialog : DialogFragment() {

    private var _binding: DialogSupportBinding? = null
    private val binding: DialogSupportBinding get() = _binding!!
    private val viewModel: SupportViewModel by viewModels()
    private var errorBody: MutableLiveData<ErrorBody> = MutableLiveData()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding = DialogSupportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObserversError()

        binding.bttSendSupportMessage.setOnClickListener {
            val enteredText = binding.enterSupportMessage.text.toString()
            if (enteredText.isNotEmpty()) {
                viewModel.sendSupportMessage(enteredText, errorBody)
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

    private fun observeSupportMessageSentStatus() {
        viewModel.supportMessageSentLiveData.observe(viewLifecycleOwner) { sent ->
            if (sent) {
                openSuccessfulMailDialog()
                dismiss()
            }
        }
    }

    private fun openSuccessfulMailDialog() {
        val generalDialog = GeneralMessageDialog(
            requireContext().getString(R.string.support_successful_mail),
            requireContext().getString(R.string.support_successful_massage)
        )
        generalDialog.show(childFragmentManager, "SupportDialog")
    }

    private fun setObserversError() {
        errorBody = MutableLiveData()
        errorBody.observe(viewLifecycleOwner) { errorBody ->
            context?.let { context ->
                Toast.makeText(
                    context,
                    errorBody.errorBody,
                    Toast.LENGTH_SHORT
                ).show()
                if (errorBody.errorCode == 405 || errorBody.errorCode == 401) {
                    MainActivity.logoutOnInvalidToken(context, findNavController())
                }
            }
        }

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