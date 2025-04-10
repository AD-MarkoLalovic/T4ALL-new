package com.mobility.enp.view.dialogs

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.mobility.enp.databinding.GeneralDialogBinding
import com.mobility.enp.util.setDimensionsPercent
import com.mobility.enp.viewmodel.FranchiseViewModel
import kotlin.getValue
import androidx.core.graphics.drawable.toDrawable

class ChangePasswordDialog(
    private val title: String,
    private val subtitle: String,
    val onConfirmButton: (Any) -> Unit
) : DialogFragment() {

    private var _binding: GeneralDialogBinding? = null
    private val binding: GeneralDialogBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        _binding = GeneralDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFranchiser()

        binding.title.text = title
        binding.subTitle.text = subtitle
        binding.confirmButton.setOnClickListener {
            dismiss()
            onConfirmButton(Any())
        }
    }

    private fun setFranchiser() {
        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.confirmButton.backgroundTintList = ColorStateList.valueOf(color)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        setDimensionsPercent(95)
        isCancelable = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}