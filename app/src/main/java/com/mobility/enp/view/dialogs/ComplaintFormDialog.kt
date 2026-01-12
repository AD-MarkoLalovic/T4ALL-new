package com.mobility.enp.view.dialogs

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputLayout
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.complaint.ComplaintBody
import com.mobility.enp.databinding.DialogComplaintFormBinding
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.util.setDimensionsPercent
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.ui_models.BankUIModel
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.PassageHistoryBanksVm

class ComplaintFormDialog() :
    DialogFragment() {

    private val viewModel: PassageHistoryBanksVm by viewModels { PassageHistoryBanksVm.Factory }
    private lateinit var binding: DialogComplaintFormBinding
    private lateinit var bankNames: MutableList<String>
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }

    private val complaintId: Int by lazy {
        requireArguments().getInt(ARG_COMPLAINT_ID)
    }

    private var onConfirmButton: ((ComplaintBody) -> Unit)? = null

    companion object{
        private const val ARG_COMPLAINT_ID = "complaint_id"
        fun newInstance(
            complaintId: Int,
            onConfirm: (ComplaintBody) -> Unit
        ): ComplaintFormDialog {
            return ComplaintFormDialog().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COMPLAINT_ID, complaintId)
                }
                onConfirmButton = onConfirm
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        binding = DialogComplaintFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpFranchise()
        observerBanks()

        binding.cancelComplaintForm.setOnClickListener {
            dialog?.dismiss()
        }

        binding.buttonConfirmComplaint.setOnClickListener {
            handleComplaintFormSubmission()
        }

        enableAccountInputs()
    }

    private fun setUpFranchise() {
        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.buttonConfirmComplaint.backgroundTintList = ColorStateList.valueOf(color)

                binding.etSecondTagPicker.background = createModifiedDrawable(color)

                val parent = binding.constraintLayout

                for (i in 0 until parent.childCount) {
                    val view = parent.getChildAt(i)

                    if (view is TextInputLayout) {
                        view.boxStrokeColor = color
                        view.boxStrokeColor = color
                        val editText = view.editText
                        editText?.textSelectHandle?.setTint(color)
                        editText?.setTextColor(color)

                        val states = arrayOf(
                            intArrayOf(android.R.attr.state_pressed),  // pressed
                            intArrayOf(android.R.attr.state_focused),  // focused
                            intArrayOf()                               // default
                        )

                        val colors = intArrayOf(
                            color,        // pressed
                            color,        // focused
                            color         // default
                        )

                        view.cursorColor = ColorStateList(states, colors)
                    }
                }
            }
        }
    }

    private fun handleComplaintFormSubmission() {
        val selectedItem = binding.uniqueNumbersSpinner.selectedItem
        val uniqueNumber = selectedItem?.toString()?.trim() ?: ""
        val centerAccountNumber = binding.etCenterAccountNumber.text.toString().trim()
        val rightAccountNumber = binding.etRightAccountNumber.text.toString().trim()
        val licencePlate = binding.licencePlateVal.text.toString().trim()
        val reasonForComplaint = binding.reasonForComplaintVal.text.toString().trim()
        val selectedBankPosition = binding.bankSpinner.selectedItemPosition

        if (licencePlate.isEmpty() || reasonForComplaint.isEmpty()) {
            showError(getString(R.string.please_enter_all_required_data))
            return
        }

        // Provera minimalne dužine razloga žalbe
        if (reasonForComplaint.length <= 10) {
            showError(getString(R.string.complaint_min_length))
            return
        }

        if (selectedBankPosition == 0) {
            showError(getString(R.string.enter_name_bank))
            return
        }

        if (uniqueNumber.isEmpty() || centerAccountNumber.isEmpty() || rightAccountNumber.isEmpty()) {
            showError(getString(R.string.enter_bank_account))
            return
        }

        // Provera dužine brojeva računa
        val isValidAccount = centerAccountNumber.length == 13 && rightAccountNumber.length == 2
        if (!isValidAccount) {
            showError(getString(R.string.invalid_account_number))
            return
        }

        dialog?.dismiss()

        val complaintBody = ComplaintBody(
            complaintId,
            reasonForComplaint,
            selectedBankPosition,
            licencePlate,
            uniqueNumber, centerAccountNumber, rightAccountNumber
        )

        onConfirmButton?.invoke(complaintBody)
    }


    override fun onStart() {
        super.onStart()
        isCancelable = false
        setDimensionsPercent(95, 80)
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
        val color = franchiseViewModel.franchiseModel.value?.franchisePrimaryColor

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

                color?.let {
                    (view as? TextView)?.setTextColor(
                        ColorStateList.valueOf(it)
                    )
                } ?: run {
                    (view as? TextView)?.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            if (position == 0) R.color.hint_text_color else R.color.figmaSplashScreenColor
                        )
                    )
                }

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
                    color?.let {
                        if (position == 0) {
                            textView?.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.hint_text_color)
                            )
                            return
                        } else {
                            textView?.setTextColor(
                                color
                            )
                        }
                    } ?: run {
                        if (position == 0) {
                            textView?.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.hint_text_color)
                            )
                            return
                        } else {
                            textView?.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.figmaSplashScreenColor
                                )
                            )
                        }
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
        val color = franchiseViewModel.franchiseModel.value?.franchisePrimaryColor

        if (uniqueNumbers.size == 1) {
            // Ako postoji samo jedan element
            val singleItem = uniqueNumbers.first().toString()
            binding.uniqueNumbersSpinner.apply {
                adapter = object : ArrayAdapter<String>(
                    requireContext(),
                    R.layout.item_unique_numbers_spinner,
                    listOf(singleItem)
                ) {
                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getView(position, convertView, parent)
                        color?.let {
                            (view as? TextView)?.setTextColor(color)

                        } ?: run {
                            (view as? TextView)?.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.figmaSplashScreenColor
                                )
                            )
                        }
                        return view
                    }
                }
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
                    color?.let {
                        (view as? TextView)?.setTextColor(
                            color
                        )
                    } ?: run {
                        (view as? TextView)?.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                if (position == 0) R.color.hint_text_color else R.color.figmaSplashScreenColor
                            )
                        )
                    }
                    return view
                }

                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    color?.let {
                        (view as? TextView)?.setTextColor(color)

                    } ?: run {
                        (view as? TextView)?.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.figmaSplashScreenColor)
                        )
                    }
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

    fun createModifiedDrawable(@ColorInt newColor: Int): StateListDrawable {
        val selectedShape = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.WHITE)
            setStroke(3, newColor)
            cornerRadii =
                floatArrayOf(0f, 0f, 84f, 84f, 84f, 84f, 0f, 0f) // top-right, bottom-right
        }

        val unselectedDrawable =
            ContextCompat.getDrawable(requireContext(), R.drawable.rounded_right_spinner_unselected)

        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_focused), selectedShape)
            addState(intArrayOf(-android.R.attr.state_focused), unselectedDrawable)
        }
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