package com.mobility.enp.view.dialogs

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.complaint.ComplaintBody
import com.mobility.enp.databinding.DialogComplaintFormBinding
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.ui_models.BankUIModel
import com.mobility.enp.viewmodel.PassageHistoryViewModelNew

class ComplaintFormDialog(val onConfirmButton: (ComplaintBody) -> Unit, complaintId: Int) :
    DialogFragment() {

    private val viewModel: PassageHistoryViewModelNew by viewModels { PassageHistoryViewModelNew.Factory }
    private lateinit var binding: DialogComplaintFormBinding
    private val id: Int = complaintId
    private lateinit var bankNames: MutableList<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = DialogComplaintFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observerBanks()

        binding.cancelComplaintForm.setOnClickListener {
            dialog?.dismiss()
        }

        binding.buttonConfirmComplaint.setOnClickListener {

            val uniqueNumber = binding.uniqueNumbersSpinner.selectedItem.toString().trim()
            val centerAccountNumber = binding.etCenterAccountNumber.text.toString().trim()
            val rightAccountNumber = binding.etRightAccountNumber.text.toString().trim()


            if (uniqueNumber.isEmpty() || centerAccountNumber.isEmpty() || rightAccountNumber.isEmpty()) {
                showError(getString(R.string.enter_bank_account))
            }

            val selectedBankPosition = binding.bankSpinner.selectedItemPosition

            if (selectedBankPosition == 0) {
                showError(getString(R.string.enter_name_bank))
            }

            if (centerAccountNumber.length == 13 && rightAccountNumber.length == 2) {

                if (binding.licencePlateVal.text.toString().isNotEmpty()
                    && binding.reasonForComplaintVal.text.toString().isNotEmpty()
                ) {

                    if (binding.reasonForComplaintVal.text.toString().length > 10) {
                        dialog?.dismiss()

                        val complaintBody = ComplaintBody(
                            id,
                            binding.reasonForComplaintVal.text.toString(),
                            selectedBankPosition,
                            binding.licencePlateVal.text.toString(),
                            uniqueNumber, centerAccountNumber, rightAccountNumber
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

            } else {
                showError("Invalid account number")
            }
        }

        enableAccountInputs()
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

                else -> {
                    binding.refundLoadingTagPicker.visibility = View.GONE
                    SubmitResult.Empty
                    SubmitResult.FailureServerError
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

        binding.bankSpinner.apply {
            adapter = bankAdapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val textView = view as? TextView
                    if (position == 0) {
                        textView?.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.hint_text_color)
                        )
                        return // Ako je hint, ne radimo ništa
                    } else {
                        textView?.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.figmaSplashScreenColor
                            ) // Plava boja za izabranu stavku
                        )
                    }

                    // Obrada odabrane stavke
                    val selectedBank = bankList[position - 1]
                    setupUniqueNumberSpinner(selectedBank.uniqueNumber)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Nema akcije
                }
            }
        }
    }

    private fun setupUniqueNumberSpinner(uniqueNumbers: List<Int>) {
        if (uniqueNumbers.size == 1) {
            // Ako postoji samo jedan element
            val singleItem = uniqueNumbers.first().toString()
            binding.uniqueNumbersSpinner.apply {
                adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.item_unique_numbers_spinner,
                    listOf(singleItem)
                )
                setSelection(0)
                isClickable = false
                isEnabled = false
            }
        } else {
            val uniqueNumberStrings = uniqueNumbers.map { it.toString() }
            val bankCodeAdapter = object : ArrayAdapter<String>(
                requireContext(),
                R.layout.item_unique_numbers_spinner_arrow,
                uniqueNumberStrings
            ) {
                override fun getDropDownView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup
                ): View {
                    val view = super.getDropDownView(position, convertView, parent)
                    view.layoutParams.height =
                        (32 * resources.displayMetrics.density).toInt() // 32dp u px
                    return view
                }
            }

            binding.uniqueNumbersSpinner.apply {
                adapter = bankCodeAdapter
                isClickable = true
                isEnabled = true
            }
        }

        enableAccountInputs()
    }

    private fun DialogFragment.setWidthPercent(percentage: Int) {
        val percent = percentage.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun enableAccountInputs() = with(binding) {    // why ?
        etCenterAccountNumber.enableEdit()
        etRightAccountNumber.enableEdit()
        etSecondTagPicker.backgroundTintList = null
        txCenterAccountNumber.setBoxBackgroundColorResource(R.color.white)
        uniqueNumbersSpinner.backgroundTintList = null
    }

    private fun View.enableEdit() {
        isClickable = true
        isFocusable = true
        isFocusableInTouchMode = true
        if (this is EditText) isCursorVisible = true
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