package com.mobility.enp.view.dialogs

import android.app.Dialog
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
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.mobility.enp.util.FragmentResultKeys

class ChangePasswordDialog() : DialogFragment() {

    private var _binding: GeneralDialogBinding? = null
    private val binding: GeneralDialogBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }

    companion object {
        private const val ARG_TITLE = "ARG_TITLE"
        private const val ARG_SUBTITLE = "ARG_SUBTITLE"

        fun newInstance(
            title: String,
            subtitle: String
        ): ChangePasswordDialog {
            return ChangePasswordDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_SUBTITLE, subtitle)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = GeneralDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFranchiser()
        val title = requireArguments().getString(ARG_TITLE)
        val subtitle = requireArguments().getString(ARG_SUBTITLE)

        binding.title.text = title
        binding.subTitle.text = subtitle

        binding.confirmButton.setOnClickListener {
            setFragmentResult(
                FragmentResultKeys.CHANGE_PASS_RESULT,
                bundleOf(FragmentResultKeys.CHANGE_PASS_CONFIRMED to true)
            )
            dismiss()
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