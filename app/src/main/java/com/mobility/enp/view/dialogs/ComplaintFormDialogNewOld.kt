package com.mobility.enp.view.dialogs

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.complaint.ComplaintBody
import com.mobility.enp.databinding.DialogComplaintFormNewOldBinding
import com.mobility.enp.util.setDimensionsPercent
import androidx.core.graphics.drawable.toDrawable

class ComplaintFormDialogNewOld(val onConfirmButton: (ComplaintBody) -> Unit, complaintId: Int) :
    DialogFragment() {

    private lateinit var binding: DialogComplaintFormNewOldBinding
    private val id: Int = complaintId

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        binding = DialogComplaintFormNewOldBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cancelComplaintForm.setOnClickListener {
            dialog?.dismiss()
        }

        binding.buttonConfirmComplaint.setOnClickListener {
            handleComplaintFormSubmission()
        }

    }

    override fun onStart() {
        super.onStart()
        isCancelable = false
        setDimensionsPercent(95)
    }

    private fun handleComplaintFormSubmission() {
        val licencePlate = binding.licencePlateVal.text.toString().trim()
        val reasonForComplaint = binding.reasonForComplaintVal.text.toString().trim()

        // Provera da li su uneti svi podaci
        if (licencePlate.isNotEmpty() && reasonForComplaint.isNotEmpty()) {
            if (reasonForComplaint.length > 10) {
                dialog?.dismiss()

                val complaintBody = ComplaintBody(
                    id,
                    reasonForComplaint,
                    null,
                    licencePlate,
                    null, null, null
                )

                onConfirmButton(complaintBody)
            } else {
                showError(getString(R.string.complaint_min_length))
            }
        } else {
            showError(getString(R.string.please_enter_all_required_data))
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}