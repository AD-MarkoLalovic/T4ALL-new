package com.mobility.enp.view.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.mobility.enp.R
import com.mobility.enp.databinding.FranchiseDialogLayoutBinding
import com.mobility.enp.viewmodel.FranchiseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FranchiseTestDialog() :
    DialogFragment() {

    private var _binding: FranchiseDialogLayoutBinding? = null
    private val binding: FranchiseDialogLayoutBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding = FranchiseDialogLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonCloseDialog.setOnClickListener { dismiss() }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val languageCode = when (checkedId) {
                R.id.telekom -> "ad7e2bb9-22a5-4184-9c9b-5c384a506cb3"
                R.id.s_blue -> "a2ac8612-4b25-43e3-8017-fcf8ad0da0c4"
                R.id.amss -> "9aa3e972-d84b-40df-b35d-d14a229c03e3"
                R.id.tehnomania -> "d47b35d1-bb44-4618-9b31-cf7e961595ec"
                else -> "null"
            }
            viewLifecycleOwner.lifecycleScope.launch {
                delay(500L)
                dialog?.dismiss()
                franchiseViewModel.upsertHomeData(languageCode)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        isCancelable = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}