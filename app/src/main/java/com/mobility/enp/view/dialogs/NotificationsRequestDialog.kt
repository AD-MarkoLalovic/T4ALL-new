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
import com.mobility.enp.databinding.NotificationsRequestDiagBinding
import com.mobility.enp.util.setDimensionsPercent
import com.mobility.enp.viewmodel.FranchiseViewModel
import androidx.core.graphics.drawable.toDrawable

class NotificationsRequestDialog : DialogFragment {

    private var title: String = ""
    private var subtitle: String = ""

    private var _binding: NotificationsRequestDiagBinding? = null
    private val binding: NotificationsRequestDiagBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }

    private lateinit var onButtonClick: OnButtonClick

    constructor() : super()

    constructor(title: String, subtitle: String, onButtonClick: OnButtonClick) : super() {
        this.title = title
        this.subtitle = subtitle
        this.onButtonClick = onButtonClick
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        _binding = NotificationsRequestDiagBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.confirmButton.backgroundTintList = ColorStateList.valueOf(color)
            }
        }

        binding.title.text = title
        binding.subTitle.text = subtitle
        binding.confirmButton.setOnClickListener {
            dialog?.dismiss()
            onButtonClick.onClickConfirmed()
        }
        binding.rejectButton.setOnClickListener {
            dialog?.dismiss()
            onButtonClick.onClickRejected()
        }
    }

    interface OnButtonClick {
        fun onClickConfirmed()
        fun onClickRejected()
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