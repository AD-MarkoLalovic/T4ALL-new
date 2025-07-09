package com.mobility.enp.view.dialogs

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.complaint.ObjectionBody
import com.mobility.enp.databinding.DialogObjectionFormBinding
import com.mobility.enp.util.SharedPreferencesHelper
import com.mobility.enp.util.Util.isTablet
import com.mobility.enp.util.setDimensionsPercent
import com.mobility.enp.viewmodel.FranchiseViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ObjectionFormDialog(private val objBody: (ObjectionBody) -> Unit, objection: Int) :
    DialogFragment() {

    private var _binding: DialogObjectionFormBinding? = null
    private val binding: DialogObjectionFormBinding get() = _binding!!

    private val id: Int = objection
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogObjectionFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setFranchise()
        setupListeners()
    }

    private fun setupUI() {
        binding.subjectNumberVal.setText(String.format(Locale.getDefault(), "%d", id))
        binding.checkbox1.buttonTintList =
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.figmaSplashScreenColor
                )
            )
        binding.checkbox1.isEnabled = false
    }

    private fun setFranchise() {
        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.bttSendObjection.backgroundTintList = ColorStateList.valueOf(color)
                binding.textView1.setTextColor(color)
                val newDrawable =
                    ContextCompat.getDrawable(requireContext(), franchiseModel.calendarResource)
                binding.complaintId.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    newDrawable,
                    null
                )

                val states = arrayOf(
                    intArrayOf(android.R.attr.state_checked),  // When switch is ON
                    intArrayOf(-android.R.attr.state_checked) // When switch is OFF
                )

                val colors = intArrayOf(
                    color,  // ON color
                    ContextCompat.getColor(requireContext(), R.color.white) // OFF color
                )

                val colorStateList = ColorStateList(states, colors)
                binding.checkbox1.buttonTintList = colorStateList

                val parent = binding.constraintLayout

                for (i in 0 until parent.childCount) {
                    val view = parent.getChildAt(i)
                    if (view is TextInputLayout) {
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

    private fun setupListeners() {
        binding.cancelComplaintForm.setOnClickListener { dialog?.dismiss() }
        binding.complaintId.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.complaintId.clearFocus()
                showDatePicker()
            }
        }

        binding.bttSendObjection.setOnClickListener { validateAndSendObjection() }
    }

    private fun showDatePicker() {
        viewLifecycleOwner.lifecycleScope.launch {
            val locale =
                when (val lang = SharedPreferencesHelper.getUserLanguage(requireContext())) {
                    "cyr" -> Locale("sr", "RS")
                    "sr", "cnr" -> Locale("sr_Latn", "RS", "Latn")
                    else -> Locale(lang)
                }

            Locale.setDefault(locale)
            val config = requireContext().resources.configuration
            config.setLocale(locale)
            requireContext().createConfigurationContext(config)

            val constraintsBuilder = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())

            franchiseViewModel.franchiseModel.value?.let { franchiseModel ->
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(requireContext().getString(R.string.select_date))
                    .setSelection(System.currentTimeMillis())
                    .setCalendarConstraints(constraintsBuilder.build())
                    .setNegativeButtonText(requireContext().getString(R.string.cancel))
                    .setPositiveButtonText(requireContext().getString(R.string.confirm))
                    .setTheme(franchiseModel.franchiseCalendarStyle)   // style gets passed here
                    .build()

                datePicker.addOnPositiveButtonClickListener {
                    try {
                        binding.complaintId.setText(convertLongToDateString(it))
                    } catch (e: Exception) {
                        Log.e("ObjectionFormDialog", "fun showDatePicker", e)
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.please_enter_date_manually),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                datePicker.show(
                    (requireContext() as AppCompatActivity).supportFragmentManager,
                    "DATE_PICKER"
                )
            } ?: run {
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(requireContext().getString(R.string.select_date))
                    .setSelection(System.currentTimeMillis())
                    .setCalendarConstraints(constraintsBuilder.build())
                    .setNegativeButtonText(requireContext().getString(R.string.cancel))
                    .setPositiveButtonText(requireContext().getString(R.string.confirm))
                    .build()

                datePicker.addOnPositiveButtonClickListener {
                    try {
                        binding.complaintId.setText(convertLongToDateString(it))
                    } catch (e: Exception) {
                        Log.e("ObjectionFormDialog", "fun showDatePicker", e)
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.please_enter_date_manually),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                datePicker.show(
                    (requireContext() as AppCompatActivity).supportFragmentManager,
                    "DATE_PICKER"
                )
            }
        }
    }

    private fun validateAndSendObjection() {
        if (binding.subjectNumberVal.text.toString().trim().isNotEmpty()
            && binding.complaintId.text.toString().trim().isNotEmpty()
            && binding.reasonForComplaintVal.text.toString().trim().isNotEmpty()
        ) {
            val options = buildOptions()
            val objection = ObjectionBody(
                id,
                binding.subjectNumberVal.text.toString().toBigInteger(),
                binding.complaintId.text.toString(),
                options,
                binding.reasonForComplaintVal.text.toString().trim()
            )
            objBody(objection)
            dialog?.dismiss()
        } else {
            showToast(getString(R.string.please_fill_all_required_data))
        }
    }

    private fun buildOptions(): String {
        return "1"
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun convertLongToDateString(time: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val date = Date(time)
        return sdf.format(date)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        isCancelable = false

        if (requireContext().isTablet()) {
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