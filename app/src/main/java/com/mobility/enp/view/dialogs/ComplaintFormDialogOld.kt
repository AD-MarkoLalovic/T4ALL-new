package com.mobility.enp.view.dialogs

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.textfield.TextInputLayout
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.complaint.ComplaintBody
import com.mobility.enp.databinding.DialogComplaintFormNewOldBinding
import com.mobility.enp.util.setDimensionsPercent
import com.mobility.enp.viewmodel.FranchiseViewModel

class ComplaintFormDialogOld() :
    DialogFragment() {

    private lateinit var binding: DialogComplaintFormNewOldBinding
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }

    private val complaintId: Int by lazy {
        requireArguments().getInt(ARG_COMPLAINT_ID)
    }

    private var onConfirmButton: ((ComplaintBody) -> Unit)? = null

    companion object {
        private const val ARG_COMPLAINT_ID = "complaint_id"
        fun newInstance(
            complaintId: Int,
            onConfirm: (ComplaintBody) -> Unit
        ): ComplaintFormDialogOld {
            return ComplaintFormDialogOld().apply {
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
        binding = DialogComplaintFormNewOldBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFranchise()

        binding.cancelComplaintForm.setOnClickListener {
            dialog?.dismiss()
        }

        binding.buttonConfirmComplaint.setOnClickListener {
            handleComplaintFormSubmission()
        }

    }

    private fun setFranchise() {
        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.buttonConfirmComplaint.backgroundTintList = ColorStateList.valueOf(color)


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
                    complaintId,
                    reasonForComplaint,
                    null,
                    licencePlate,
                    null, null, null
                )

                onConfirmButton?.invoke(complaintBody)
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