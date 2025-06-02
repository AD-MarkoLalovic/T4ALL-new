package com.mobility.enp.view.dialogs

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.mobility.enp.databinding.SerbianTagInCroatiaDialogBinding
import com.mobility.enp.util.setDimensionsPercent
import com.mobility.enp.viewmodel.FranchiseViewModel

class SerbianTagInCroatiaDialog : DialogFragment() {

    private var _binding: SerbianTagInCroatiaDialogBinding? = null
    private val binding: SerbianTagInCroatiaDialogBinding get() = _binding!!

    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SerbianTagInCroatiaDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.let { data ->
                binding.bttCloseNote.backgroundTintList =
                    ColorStateList.valueOf(data.franchisePrimaryColor)
                binding.tagRegistrationPlateNotice.setTextColor(ColorStateList.valueOf(data.franchisePrimaryColor))

                binding.noteClose.setImageResource(data.franchiseCloseButton)
            }
        }

        binding.noteClose.setOnClickListener {
            dismiss()
        }

        binding.bttCloseNote.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        setDimensionsPercent(95, 80)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}