package com.mobility.enp.view.dialogs

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.mobility.enp.databinding.DialogLostTagBinding
import com.mobility.enp.util.setDimensionsPercent
import com.mobility.enp.viewmodel.FranchiseViewModel
import kotlin.getValue
import androidx.core.graphics.drawable.toDrawable

class LostTagDialog : DialogFragment {

    private var _binding: DialogLostTagBinding? = null
    private val binding: DialogLostTagBinding get() = _binding!!

    private lateinit var title: String
    private lateinit var subtitle: String
    private lateinit var onButtonClickInLostTag: OnButtonClickInLostTag
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }

    constructor() : super()

    constructor(
        title: String,
        subtitle: String,
        onButtonClickInLostTag: OnButtonClickInLostTag
    ) : super() {
        this.title = title
        this.subtitle = subtitle
        this.onButtonClickInLostTag = onButtonClickInLostTag
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

        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.buttonConfirmLostTag.backgroundTintList = ColorStateList.valueOf(color)
            }
        }

        binding.titleLostTag.text = title
        binding.subtitleLostTag.text = subtitle

        binding.buttonConfirmLostTag.setOnClickListener {

            onButtonClickInLostTag.onClickConfirmed()
            dismiss()
        }

        binding.bttCancelLostTag.setOnClickListener {
            dismiss()
        }

    }

    interface OnButtonClickInLostTag {
        fun onClickConfirmed()

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