package com.mobility.enp.view.dialogs

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.complaint.ObjectionBody
import com.mobility.enp.databinding.DialogObjectionFormBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ObjectionFormDialog(private val objBody: (ObjectionBody) -> Unit, objection: Int) :
    DialogFragment() {

    private lateinit var binding: DialogObjectionFormBinding
    private val id: Int = objection

    private val stringBuild = StringBuilder()

    private var checkbox1: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = DialogObjectionFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cancelComplaintForm.setOnClickListener {
            dialog?.dismiss()
        }

        binding.complaintId.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {  // show

                val fragmentManager = (context as AppCompatActivity).supportFragmentManager

                val datePicker =
                    MaterialDatePicker.Builder.datePicker()
                        .setTitleText(getString(R.string.select_date))
                        .setSelection(System.currentTimeMillis())
                        .setNegativeButtonText(getString(R.string.cancel))
                        .setPositiveButtonText(getString(R.string.confirm))
                        .build()


                datePicker.addOnPositiveButtonClickListener {// time in long
                    try {
                        val x: String = convertLongToDateString(it)
                        binding.complaintId.setText(x)
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            getString(R.string.please_enter_date_manually), Toast.LENGTH_SHORT
                        ).show()
                    }
                }


                datePicker.show(fragmentManager, "datePicker")

            }
        }

        binding.buttonConfirmLostTag.setOnClickListener {
            if (checkbox1) {
                if (binding.subjectNumberVal.text.toString().trim().isNotEmpty()
                    && binding.complaintId.text.toString().trim().isNotEmpty()
                    && binding.reasonForComplaintVal.text.toString().trim().isNotEmpty()
                ) {
                    if (checkbox1) {
                        stringBuild.append("1")
                    }

                    val options = stringBuild.toString()

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
                    Toast.makeText(
                        context,
                        getString(R.string.please_fill_all_required_data), Toast.LENGTH_SHORT
                    ).show()
                }

            } else {
                Toast.makeText(
                    context,
                    getString(R.string.please_fill_all_required_data), Toast.LENGTH_SHORT
                ).show()
            }
        }

        val checkBoxListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            // Handle checkbox state change here
            when (buttonView.id) {
                R.id.checkbox1 -> {
                    checkbox1 = isChecked
                }
            }
        }


        binding.checkbox1.setOnCheckedChangeListener(checkBoxListener)
        val blueColor = ColorStateList.valueOf(resources.getColor(R.color.figmaSplashScreenColor))
        binding.checkbox1.isChecked = true
        binding.checkbox1.buttonTintList = blueColor
        binding.checkbox1.isEnabled = false
    }

    private fun convertLongToDateString(time: Long): String {
        val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH)
        val date = Date(time)
        return sdf.format(date)
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