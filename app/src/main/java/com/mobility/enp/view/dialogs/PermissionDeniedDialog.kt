package com.mobility.enp.view.dialogs

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.mobility.enp.databinding.DialogPermissionDeniedBinding
import com.mobility.enp.util.setDimensionsPercent
import com.mobility.enp.viewmodel.FranchiseViewModel
import kotlin.getValue


class PermissionDeniedDialog : DialogFragment() {

    private var _binding: DialogPermissionDeniedBinding? = null
    private val binding: DialogPermissionDeniedBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }

    companion object {
        private const val ARG_TITLE = "ARG_TITLE"
        private const val ARG_SUBTITLE = "ARG_SUBTITLE"
        private const val ARG_RESULT_KEY = "ARG_RESULT_KEY"
        private const val ARG_RESULT_VALUE_KEY = "ARG_RESULT_VALUE_KEY"

        fun newInstance(
            title: String,
            subtitle: String,
            resultKey: String,
            resultValueKey: String
        ): PermissionDeniedDialog {
            return PermissionDeniedDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_SUBTITLE, subtitle)
                    putString(ARG_RESULT_KEY, resultKey)
                    putString(ARG_RESULT_VALUE_KEY, resultValueKey)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            isCancelable = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogPermissionDeniedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFranchiser()

        val title = requireArguments().getString(ARG_TITLE)
        val subtitle = requireArguments().getString(ARG_SUBTITLE)
        val resultKey = requireArguments().getString(ARG_RESULT_KEY)!!
        val resultValueKey = requireArguments().getString(ARG_RESULT_VALUE_KEY)!!

        binding.titlePermissionDenied.text = title
        binding.subtitlePermissionDenied.text = subtitle

        binding.bttCancelPermissionDenied.setOnClickListener {
            dismiss()
        }

        binding.bttSettings.setOnClickListener {
            setFragmentResult(
                resultKey,
                bundleOf(resultValueKey to true)
            )
            dismiss()
        }
    }

    private fun setFranchiser() {
        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.bttSettings.backgroundTintList = ColorStateList.valueOf(color)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        setDimensionsPercent(95)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}