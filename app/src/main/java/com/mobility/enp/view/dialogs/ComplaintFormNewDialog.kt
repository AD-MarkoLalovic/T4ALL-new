package com.mobility.enp.view.dialogs

import android.app.Dialog
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
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputLayout
import com.mobility.enp.R
import com.mobility.enp.data.model.new_toll_history.complaint.ComplaintBodyNew
import com.mobility.enp.databinding.DialogComplaintFormNewBinding
import com.mobility.enp.util.FragmentResultKeys
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.Util.isTablet
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.util.setDimensionsPercent
import com.mobility.enp.util.toast
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.ui_models.BankUIModel
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.toll_history.ComplaintValidationResult
import com.mobility.enp.viewmodel.toll_history.ComplaintViewModel
import kotlin.getValue

class ComplaintFormNewDialog : DialogFragment() {

    private var _binding: DialogComplaintFormNewBinding? = null
    private val binding: DialogComplaintFormNewBinding get() = _binding!!

    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val viewModel: ComplaintViewModel by viewModels { ComplaintViewModel.factory }

    private val args: ComplaintFormNewDialogArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogComplaintFormNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.containerBankForm.isVisible = args.showBankForm
        setUpFranchise()
        binding.cancelComplaintForm.setOnClickListener { findNavController().navigateUp() }
        binding.buttonConfirmComplaint.setOnClickListener {
            handleComplaintFormSubmission(args.itemId, args.showBankForm)
        }

        if (args.showBankForm) {
            collectLatestLifecycleFlow(viewModel.banks) { banks ->
                setupBankSpinner(banks)
            }
        }

        collectLatestLifecycleFlow(viewModel.submitComplaint) { state ->
            when (state) {
                is SubmitResult.Loading -> {
                    binding.complaintProgressBar.visibility = View.VISIBLE
                    binding.buttonConfirmComplaint.isEnabled = false
                }

                is SubmitResult.Success -> {
                    binding.complaintProgressBar.visibility = View.GONE
                    parentFragmentManager.setFragmentResult(
                        FragmentResultKeys.COMPLAINT_SUBMITTED_RESULT,
                        Bundle().apply {
                            putBoolean(FragmentResultKeys.COMPLAINT_SUBMITTED_KEY, true)
                        }
                    )
                    findNavController().navigateUp()
                }

                is SubmitResult.FailureApiError -> {
                    binding.complaintProgressBar.visibility = View.GONE
                    binding.buttonConfirmComplaint.isEnabled = true
                    toast(state.errorMessage)
                }

                is SubmitResult.InvalidApiToken -> {
                    binding.complaintProgressBar.visibility = View.GONE
                    MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
                }

                is SubmitResult.FailureNoConnection -> {
                    binding.complaintProgressBar.visibility = View.GONE
                    binding.buttonConfirmComplaint.isEnabled = true
                    toast(getString(R.string.no_internet))
                }

                is SubmitResult.FailureServerError -> {
                    binding.complaintProgressBar.visibility = View.GONE
                    binding.buttonConfirmComplaint.isEnabled = true
                    toast(getString(R.string.server_error_msg))
                }

                is SubmitResult.Empty -> Unit
            }
        }
    }

    private fun setupBankSpinner(bankList: List<BankUIModel>) {
        val isEmpty = bankList.isEmpty()

        binding.bankSpinner.apply {
            isEnabled = !isEmpty
            isClickable = !isEmpty
            alpha = if (isEmpty) 0.5f else 1f
        }

        if (isEmpty) return

        // Dodavanje hint-a na početak liste
        val bankNames = buildList {
            add(getString(R.string.hint_select_bank))
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
                (view as? TextView)?.applyColor(
                    color = color,
                    fallback = if (position == 0) R.color.hint_text_color
                    else R.color.figmaSplashScreenColor
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
                    (view as? TextView)?.let { textView ->
                        if (position == 0) {
                            textView.applyColor(null, R.color.hint_text_color)
                        } else {
                            textView.applyColor(color, R.color.figmaSplashScreenColor)
                        }
                    }

                    if (position <= 0) return

                    // Obrada odabrane stavke
                    val selectedBank = bankList[position - 1]
                    setupUniqueNumberSpinner(selectedBank.uniqueNumber)
                }

                override fun onNothingSelected(parent: AdapterView<*>) = Unit
            }
        }
    }

    private fun handleComplaintFormSubmission(complaintId: Int, showBankForm: Boolean) {
        val licencePlate        = binding.licencePlateVal.text.toString().trim()
        val reasonForComplaint  = binding.reasonForComplaintVal.text.toString().trim()
        val selectedBankPosition = binding.bankSpinner.selectedItemPosition
        val uniqueNumber        = binding.uniqueNumbersSpinner.selectedItem?.toString()?.trim() ?: ""
        val centerAccountNumber = binding.etCenterAccountNumber.text.toString().trim()
        val rightAccountNumber  = binding.etRightAccountNumber.text.toString().trim()

        val validation = viewModel.validate(
            licencePlate        = licencePlate,
            reasonForComplaint  = reasonForComplaint,
            showBankForm        = showBankForm,
            selectedBankPosition = selectedBankPosition,
            uniqueNumber        = uniqueNumber,
            centerAccountNumber = centerAccountNumber,
            rightAccountNumber  = rightAccountNumber
        )

        val errorRes = when (validation) {
            ComplaintValidationResult.Valid                -> null
            ComplaintValidationResult.EmptyRequiredFields  -> R.string.please_enter_all_required_data
            ComplaintValidationResult.ReasonTooShort       -> R.string.complaint_min_length
            ComplaintValidationResult.NoBankSelected       -> R.string.enter_name_bank
            ComplaintValidationResult.MissingBankFields    -> R.string.enter_bank_account
            ComplaintValidationResult.InvalidAccountNumber -> R.string.invalid_account_number
        }

        if (errorRes != null) {
            toast(getString(errorRes))
            return
        }

        val body = if (showBankForm) {
            ComplaintBodyNew(
                itemId = complaintId,
                complaintRegistration = licencePlate,
                complaintText = reasonForComplaint,
                complaintBankName = selectedBankPosition,
                accountZr = uniqueNumber,
                accountZr2 = centerAccountNumber,
                accountZr3 = rightAccountNumber
            )
        } else {
            ComplaintBodyNew(
                itemId = complaintId,
                complaintRegistration = licencePlate,
                complaintText = reasonForComplaint
            )
        }

        viewModel.submitComplaint(body)
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
                        (view as? TextView)?.applyColor(
                            color = color,
                            fallback = R.color.figmaSplashScreenColor
                        )

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

                    (view as? TextView)?.applyColor(
                        color = color,
                        fallback = if (position == 0) R.color.hint_text_color
                        else R.color.figmaSplashScreenColor
                    )

                    return view
                }

                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)

                    (view as? TextView)?.applyColor(
                        color = color,
                        fallback = R.color.figmaSplashScreenColor
                    )

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

    private fun enableAccountInputs() = with(binding) {
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
                        val editText = view.editText
                        editText?.textSelectHandle?.setTint(color)
                        editText?.setTextColor(color)

                        val states = arrayOf(
                            intArrayOf(android.R.attr.state_pressed),
                            intArrayOf(android.R.attr.state_focused),
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

    private fun createModifiedDrawable(@ColorInt newColor: Int): StateListDrawable {
        val selectedShape = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.WHITE)
            setStroke(3, newColor)
            cornerRadii =
                floatArrayOf(0f, 0f, 84f, 84f, 84f, 84f, 0f, 0f)
        }

        val unselectedDrawable =
            ContextCompat.getDrawable(requireContext(), R.drawable.rounded_right_spinner_unselected)

        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_focused), selectedShape)
            addState(intArrayOf(-android.R.attr.state_focused), unselectedDrawable)
        }
    }

    private fun TextView.applyColor(@ColorInt color: Int?, @ColorRes fallback: Int) {
        setTextColor(
            color ?: ContextCompat.getColor(requireContext(), fallback)
        )
    }

    override fun onStart() {
        super.onStart()
        val isTablet = requireContext().isTablet()

        if (isTablet) {
            setDimensionsPercent(95)
        } else {
            setDimensionsPercent(95, if (args.showBankForm) 80 else null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}