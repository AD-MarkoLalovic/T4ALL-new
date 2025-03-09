package com.mobility.enp.view.dialogs

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.complaint.ObjectionBody
import com.mobility.enp.databinding.DialogObjectionFormBinding
import com.mobility.enp.util.setDimensionsPercent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ObjectionFormDialog(private val objBody: (ObjectionBody) -> Unit, objection: Int) :
    DialogFragment() {

    private var _binding: DialogObjectionFormBinding? = null
    private val binding: DialogObjectionFormBinding get() = _binding!!
    private val id: Int = objection


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        _binding = DialogObjectionFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        binding.subjectNumberVal.setText(String.format(Locale.getDefault(), "%d", id))
        binding.checkbox1.buttonTintList =
            ColorStateList.valueOf(resources.getColor(R.color.figmaSplashScreenColor))
        binding.checkbox1.isEnabled = false
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
        val fragmentManager = (requireContext() as AppCompatActivity).supportFragmentManager
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.select_date))
            .setSelection(System.currentTimeMillis())
            .setNegativeButtonText(getString(R.string.cancel))
            .setPositiveButtonText(getString(R.string.confirm))
            .build()

        datePicker.addOnPositiveButtonClickListener {
            try {
                binding.complaintId.setText(convertLongToDateString(it))
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_enter_date_manually),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        datePicker.show(fragmentManager, "datePicker")
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
        setDimensionsPercent(95, 80)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}