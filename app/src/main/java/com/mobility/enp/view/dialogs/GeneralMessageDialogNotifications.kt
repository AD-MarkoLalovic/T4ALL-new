package com.mobility.enp.view.dialogs

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.mobility.enp.databinding.GeneralDialogNotificationsBinding
import com.mobility.enp.util.setDimensionsPercent
import com.mobility.enp.viewmodel.FranchiseViewModel

class GeneralMessageDialogNotifications : DialogFragment {

    private var _binding: GeneralDialogNotificationsBinding? = null
    private val binding: GeneralDialogNotificationsBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }

    private var onButtonClick: OnButtonClick? = null

    fun setOnButtonClickListener(listener: OnButtonClick) {
        this.onButtonClick = listener
    }

    private fun handleClick() {
        onButtonClick?.onClickConfirmed()
    }

    constructor() : super()

    companion object {
        private const val ARG_TITLE = "ARG_TITLE"
        private const val ARG_SUBTITLE = "ARG_SUBTITLE"

        fun newInstance(
            title: String,
            subtitle: String,
        ): GeneralMessageDialogNotifications {
            return GeneralMessageDialogNotifications().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_SUBTITLE, subtitle)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding = GeneralDialogNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.confirmButton.backgroundTintList = ColorStateList.valueOf(color)
            }
        }

        binding.title.text = requireArguments().getString(ARG_TITLE).orEmpty()
        binding.subTitle.text = requireArguments().getString(ARG_SUBTITLE).orEmpty()

        binding.confirmButton.setOnClickListener {
            dialog?.dismiss()
            onButtonClick?.onClickConfirmed()
        }

        binding.rejectButton.setOnClickListener {
            dismiss()
        }
    }

    interface OnButtonClick {
        fun onClickConfirmed()
    }

    override fun onStart() {
        super.onStart()
        setDimensionsPercent(85)
        isCancelable = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}