package com.mobility.enp.view.dialogs

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.complaint.ComplaintBody
import com.mobility.enp.databinding.DialogComplaintFormNewOldBinding
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.ui_models.BankUIModel
import com.mobility.enp.viewmodel.PassageHistoryBanksVm

class ComplaintFormDialogNewOld(val onConfirmButton: (ComplaintBody) -> Unit, complaintId: Int) :
    DialogFragment() {

    private val viewModel: PassageHistoryBanksVm by viewModels { PassageHistoryBanksVm.Factory }
    private lateinit var binding: DialogComplaintFormNewOldBinding
    private val id: Int = complaintId
    private lateinit var bankNames: MutableList<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = DialogComplaintFormNewOldBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cancelComplaintForm.setOnClickListener {
            dialog?.dismiss()
        }

        binding.buttonConfirmComplaint.setOnClickListener {


            if (binding.licencePlateVal.text.toString().isNotEmpty()
                && binding.reasonForComplaintVal.text.toString().isNotEmpty()
            ) {

                if (binding.reasonForComplaintVal.text.toString().length > 10) {
                    dialog?.dismiss()

                    val complaintBody = ComplaintBody(
                        id,
                        binding.reasonForComplaintVal.text.toString(),
                        null,
                        binding.licencePlateVal.text.toString(),
                        null, null, null
                    )

                    onConfirmButton(complaintBody)

                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.complaint_min_length),
                        Toast.LENGTH_SHORT
                    ).show()
                }


            } else {
                Toast.makeText(
                    context,
                    getString(R.string.please_enter_all_required_data), Toast.LENGTH_SHORT
                ).show()
            }

        }

    }

    override fun onStart() {
        super.onStart()
        setWidthPercent(95)
    }

    private fun observerBanks() {
        collectLatestLifecycleFlow(viewModel.banks) { bank ->
            when (bank) {
                SubmitResult.Loading -> {
                    binding.refundLoadingTagPicker.visibility = View.VISIBLE
                }

                is SubmitResult.FailureApiError -> {
                    binding.refundLoadingTagPicker.visibility = View.GONE
                    showError(bank.errorMessage)
                }

                SubmitResult.FailureNoConnection -> showNoConnectionState()

                is SubmitResult.Success -> {
                    binding.refundLoadingTagPicker.visibility = View.GONE
                    setupBankSpinner(bank.data)
                }

                SubmitResult.FailureServerError -> {
                    showError(getString(R.string.server_error_msg))
                }

                else -> {
                    binding.refundLoadingTagPicker.visibility = View.GONE
                    SubmitResult.Empty
                }
            }

        }
    }

    private fun setupBankSpinner(bankList: List<BankUIModel>) {
        // Dodavanje hint-a na početak liste
        bankNames = mutableListOf(getString(R.string.hint_select_bank)).apply {
            addAll(bankList.map { it.bankName })
        }

        // Adapter za spinner
        val bankAdapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.item_banks_spinner,
            bankNames
        ) {
            // Onemogućiti klik na poziciju 0, tj. hint
            override fun isEnabled(position: Int) = position != 0

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as? TextView)?.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (position == 0) R.color.hint_text_color else R.color.figmaSplashScreenColor
                    )
                )
                return view
            }

        }

    }

    private fun DialogFragment.setWidthPercent(percentage: Int) {
        val percent = percentage.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showNoConnectionState() {
        binding.refundLoadingTagPicker.visibility = View.GONE
        noInternetMessage()
    }

    private fun noInternetMessage() {
        val mainBinding = (activity as MainActivity).binding
        MainActivity.showSnackMessage(getString(R.string.no_internet), mainBinding)
    }
}