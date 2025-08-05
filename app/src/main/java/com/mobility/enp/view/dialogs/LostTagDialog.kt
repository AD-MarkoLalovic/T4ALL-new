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
import com.mobility.enp.databinding.DialogLostTagBinding
import com.mobility.enp.util.setDimensionsPercent
import com.mobility.enp.viewmodel.FranchiseViewModel

class LostTagDialog : DialogFragment() {

    private var _binding: DialogLostTagBinding? = null
    private val binding: DialogLostTagBinding get() = _binding!!

    private var onButtonClick: (() -> Unit)? = null
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }

    companion object {
        private const val ARG_TITLE = "ARG_TITLE"
        private const val ARG_SUBTITLE = "ARG_SUBTITLE"

        fun newInstance(
            title: String,
            subtitle: String,
            onButtonClick: () -> Unit
        ): LostTagDialog {
            val dialog = LostTagDialog()
            dialog.arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_SUBTITLE, subtitle)
            }
            dialog.onButtonClick = onButtonClick
            return dialog
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        _binding = DialogLostTagBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = requireArguments().getString(ARG_TITLE).orEmpty()
        val subtitle = requireArguments().getString(ARG_SUBTITLE).orEmpty()

        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.buttonConfirmLostTag.backgroundTintList = ColorStateList.valueOf(color)
            }
        }

        binding.titleLostTag.text = title
        binding.subtitleLostTag.text = subtitle

        binding.buttonConfirmLostTag.setOnClickListener {
            onButtonClick?.invoke()
            dismiss()
        }

        binding.bttCancelLostTag.setOnClickListener {
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