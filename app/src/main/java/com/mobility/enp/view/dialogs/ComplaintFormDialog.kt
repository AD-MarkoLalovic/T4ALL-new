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
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.complaint.ComplaintBody
import com.mobility.enp.databinding.DialogComplaintFormBinding

class ComplaintFormDialog(apiInterface: OnClick, complaintId: Int) : DialogFragment() {

    private lateinit var binding: DialogComplaintFormBinding
    private val sendApiInterface: OnClick = apiInterface
    private val id: Int = complaintId

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

        binding.cancelComplaintForm.setOnClickListener {
            dialog?.dismiss()
        }

        binding.buttonConfirmComplaint.setOnClickListener {
            if (binding.licencePlateVal.text.toString().isNotEmpty()
                && binding.reasonForComplaintVal.text.toString().isNotEmpty()
                && binding.accountNumberVal.text.toString().isNotEmpty()
                && binding.bankNameVal.text.toString().isNotEmpty()
            ) {

                if (binding.reasonForComplaintVal.text.toString().length > 10) {
                    dialog?.dismiss()
                    sendApiInterface.postComplaint(
                        ComplaintBody(
                            id,
                            binding.reasonForComplaintVal.text.toString(),
                            binding.accountNumberVal.text.toString(),
                            binding.bankNameVal.text.toString(),
                            binding.licencePlateVal.text.toString()
                        )
                    )
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


    interface OnClick {
        fun postComplaint(complaintBody: ComplaintBody)
    }

    override fun onStart() {
        super.onStart()
        setWidthPercent(95)
    }

    private fun DialogFragment.setWidthPercent(percentage: Int) {
        val percent = percentage.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

}