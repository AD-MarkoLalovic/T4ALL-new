package com.mobility.enp.view.dialogs

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.mobility.enp.R
import com.mobility.enp.databinding.GeneralDialogHistoryInfoBinding
import com.mobility.enp.util.setDimensionsPercent
import com.mobility.enp.viewmodel.FranchiseViewModel
import org.checkerframework.checker.units.qual.s

class GeneralMessageDialogInfoButton() : DialogFragment() {

    private var _binding: GeneralDialogHistoryInfoBinding? = null
    private val binding: GeneralDialogHistoryInfoBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }

    companion object {
        private const val ARG_TITLE = "ARG_TITLE"
        private const val ARG_SUBTITLE = "ARG_SUBTITLE"
        private const val ARG_RESULT_KEY = "ARG_RESULT_KEY"
        private const val ARG_RESULT_VALUE_KEY = "ARG_RESULT_VALUE_KEY"

        fun newInstance(
            title: String,
            subtitle: String,
            resultKey: String? = null,
            resultValueKey: String? = null
        ): GeneralMessageDialogInfoButton {
            return GeneralMessageDialogInfoButton().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_SUBTITLE, subtitle)
                    putString(ARG_RESULT_KEY, resultKey)
                    putString(ARG_RESULT_VALUE_KEY, resultValueKey)
                }
            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        _binding = GeneralDialogHistoryInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = requireArguments().getString(ARG_TITLE).orEmpty()
        val subtitle = requireArguments().getString(ARG_SUBTITLE).orEmpty()
        val resultKey = requireArguments().getString(ARG_RESULT_KEY)
        val resultValueKey = requireArguments().getString(ARG_RESULT_VALUE_KEY)

        binding.title.text = title

        val spannable = SpannableString(subtitle)

        val token = "*"
        val iconPosition = subtitle.indexOf(token)
        if (iconPosition == -1) return

        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.loop)

        drawable?.setBounds(0, 0, 60, 60)

        val imageSpan = ImageSpan(drawable!!, ImageSpan.ALIGN_BOTTOM)

        spannable.setSpan(
            imageSpan,
            iconPosition,
            iconPosition + 1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.subTitle.text = spannable

        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.confirmButton.backgroundTintList = ColorStateList.valueOf(color)
            }
        }

        binding.confirmButton.setOnClickListener {
            if (resultKey != null && resultValueKey != null) {
                setFragmentResult(
                    resultKey,
                    bundleOf(resultValueKey to true)
                )
            }
            dismiss()
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