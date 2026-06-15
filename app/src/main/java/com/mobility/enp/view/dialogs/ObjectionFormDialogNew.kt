package com.mobility.enp.view.dialogs

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.mobility.enp.R
import com.mobility.enp.data.model.new_toll_history.objection.ObjectionBodyNew
import com.mobility.enp.databinding.DialogObjectionFormNewBinding
import com.mobility.enp.util.FragmentResultKeys
import android.content.res.ColorStateList
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.Util.isTablet
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.util.setDimensionsPercent
import com.mobility.enp.util.toast
import com.mobility.enp.view.MainActivity
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.toll_history.ObjectionFormViewModel
import com.mobility.enp.viewmodel.toll_history.ObjectionValidationResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.getValue

class ObjectionFormDialogNew : DialogFragment() {

    private var _binding: DialogObjectionFormNewBinding? = null
    private val binding: DialogObjectionFormNewBinding get() = _binding!!

    private val viewModel: ObjectionFormViewModel by viewModels { ObjectionFormViewModel.factory }
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }

    private val args: ObjectionFormDialogNewArgs by navArgs()

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
        _binding = DialogObjectionFormNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUi()
        setFranchise()
        observeState()
        setupListeners()

    }

    private fun setupUi() {
        binding.objectionNumber.setText(args.complaintId.toString())

        binding.checkbox1.apply {
            isChecked = true
            isEnabled = false
            isClickable = false
            buttonTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.figmaSplashScreenColor)
            )
        }
    }

    private fun observeState() {
        collectLatestLifecycleFlow(viewModel.submitObjectionState) { result ->
            when(result) {
                SubmitResult.Empty -> Unit
                is SubmitResult.FailureApiError -> {
                    binding.objectionNewProgressBar.visibility = View.GONE
                    binding.bttSendObjection.isEnabled = true
                    toast(result.errorMessage)
                }
                SubmitResult.FailureNoConnection -> {
                    binding.objectionNewProgressBar.visibility = View.GONE
                    binding.bttSendObjection.isEnabled = true
                    toast(getString(R.string.no_internet))
                }
                SubmitResult.FailureServerError -> {
                    binding.objectionNewProgressBar.visibility = View.GONE
                    binding.bttSendObjection.isEnabled = true
                    toast(getString(R.string.server_error_msg))
                }
                is SubmitResult.InvalidApiToken -> {
                    binding.objectionNewProgressBar.visibility = View.GONE
                    MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
                }
                SubmitResult.Loading -> {
                    binding.objectionNewProgressBar.visibility = View.VISIBLE
                    binding.bttSendObjection.isEnabled = true
                }
                is SubmitResult.Success -> {
                    binding.objectionNewProgressBar.visibility = View.GONE
                    parentFragmentManager.setFragmentResult(
                        FragmentResultKeys.OBJECTION_SUBMITTED_RESULT,
                        Bundle().apply {
                            putBoolean(FragmentResultKeys.OBJECTION_SUBMITTED_KEY, true)
                        }
                    )
                    findNavController().navigateUp()
                }
            }

        }
    }

    private fun setupListeners() {
        binding.cancelObjectionForm.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.editDataPicker.setOnClickListener { showDatePicker() }

        binding.bttSendObjection.setOnClickListener { handleObjectionFormSubmission(args.complaintId) }
    }

    private fun handleObjectionFormSubmission(complaintId: Int) {
        val objectionItemDate = binding.editDataPicker.text.toString().trim()
        val objectionItemReason = binding.reasonForObjection.text.toString().trim()

        val validation = viewModel.validateObjectionForm(objectionItemDate, objectionItemReason)

        val errorResult = when (validation) {
            ObjectionValidationResult.EmptyRequiredFields -> R.string.please_enter_all_required_data
            ObjectionValidationResult.ReasonTooShort -> R.string.complaint_min_length
            ObjectionValidationResult.Valid -> null
        }

        if (errorResult != null) {
            toast(getString(errorResult))
            return
        }

        val body = ObjectionBodyNew(
            complaintRequestId = complaintId,
            objectionItemDate = objectionItemDate,
            objectionItemNumber = complaintId.toString(),
            objectionItemReason = objectionItemReason
        )

        viewModel.submitObjection(body)
    }

    private fun showDatePicker() {
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())
        val theme = franchiseViewModel.franchiseModel.value?.franchiseCalendarStyle
        val datePicker = buildDatePicker(constraintsBuilder.build(), theme)

        datePicker.addOnPositiveButtonClickListener { selectedMs ->
            binding.editDataPicker.setText(convertLongToDateString(selectedMs))
        }
        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun buildDatePicker(
        constraints: CalendarConstraints,
        theme: Int?
    ): MaterialDatePicker<Long> {
        val builder = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.select_date))
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setCalendarConstraints(constraints)
            .setNegativeButtonText(getString(R.string.cancel))
            .setPositiveButtonText(getString(R.string.confirm))
        if (theme != null) builder.setTheme(theme)
        return builder.build()
    }

    private fun convertLongToDateString(time: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val date = Date(time)
        return sdf.format(date)
    }

    private fun setFranchise() {
        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.bttSendObjection.backgroundTintList = ColorStateList.valueOf(color)
                binding.textView1.setTextColor(color)

                val calendarDrawable =
                    ContextCompat.getDrawable(requireContext(), franchiseModel.calendarResource)
                binding.editDataPicker.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    calendarDrawable,
                    null
                )

                binding.checkbox1.buttonTintList = ColorStateList.valueOf(color)

                val parent = binding.constraintLayout
                for (i in 0 until parent.childCount) {
                    val view = parent.getChildAt(i)
                    if (view is TextInputLayout) {
                        view.boxStrokeColor = color
                        val editText = view.editText
                        editText?.textSelectHandle?.setTint(color)
                        editText?.setTextColor(color)
                        view.cursorColor = createFranchiseCursorColor(color)
                    }
                }
            }
        }
    }

    private fun createFranchiseCursorColor(@ColorInt color: Int): ColorStateList =
        ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_pressed),
                intArrayOf(android.R.attr.state_focused),
                intArrayOf()
            ),
            intArrayOf(color, color, color)
        )

    override fun onStart() {
        super.onStart()
        val isTablet = requireContext().isTablet()

        if (isTablet) {
            setDimensionsPercent(95)
        } else {
            setDimensionsPercent(95, 80)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}