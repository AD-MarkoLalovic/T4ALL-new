package com.mobility.enp.view.dialogs

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.mobility.enp.R
import com.mobility.enp.databinding.LanguageDialogLayoutBinding
import com.mobility.enp.util.FragmentResultKeys
import com.mobility.enp.util.SharedPreferencesHelper
import com.mobility.enp.viewmodel.FranchiseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LanguageDialog : DialogFragment() {

    private var _binding: LanguageDialogLayoutBinding? = null
    private val binding: LanguageDialogLayoutBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }

    private var previousLanguageCode = ""
    private var isLanguageChanging = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        _binding = LanguageDialogLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        previousLanguageCode = SharedPreferencesHelper.getUserLanguage(requireContext())

        updateSelectedLanguage(previousLanguageCode)
        setFranchise()

        binding.buttonCloseDialog.setOnClickListener {
            if (!isLanguageChanging) {
                dismiss()
            }
        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->

            if (isLanguageChanging) return@setOnCheckedChangeListener

            val languageCode = when (checkedId) {
                R.id.languageEnglish -> "en"
                R.id.languageSerbian -> "cyr"
                R.id.languageSerbianLatin -> "sr"
                R.id.languageMontenegro -> "cnr"
                R.id.languageMacedonian -> "mk"
                R.id.languageBosnia -> "bs"
                R.id.languageCroatia -> "hr"
                R.id.languageGerman -> "de"
                R.id.languageTurki -> "tr"
                R.id.languageGreek -> "el"
                else -> null
            }

            languageCode?.let { code ->
                previousLanguageCode = code

                isLanguageChanging = true
                binding.buttonCloseDialog.isEnabled = false

                viewLifecycleOwner.lifecycleScope.launch {
                    delay(500L)

                    parentFragmentManager.setFragmentResult(
                        FragmentResultKeys.LANGUAGE_DIALOG_RESULT,
                        bundleOf(
                            FragmentResultKeys.LANGUAGE_DIALOG_KEY to code,
                            FragmentResultKeys.LANGUAGE_CAN_SWITCH to true
                        )
                    )
                    dismiss()
                }
            }
        }
    }

    private fun setFranchise() {
        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.buttonCloseDialog.backgroundTintList = ColorStateList.valueOf(color)

                val parent = binding.radioGroup
                for (i in 0 until parent.childCount) {
                    val view = parent.getChildAt(i)
                    if (view is RadioButton) {
                        view.buttonTintList = franchiseModel.navHomeDrawable
                    }
                }
            }
        }
    }

    private fun updateSelectedLanguage(languageCode: String) {
        when (languageCode) {
            "cyr" -> binding.languageSerbian.isChecked = true
            "sr" -> binding.languageSerbianLatin.isChecked = true
            "en" -> binding.languageEnglish.isChecked = true
            "cnr" -> binding.languageMontenegro.isChecked = true
            "mk" -> binding.languageMacedonian.isChecked = true
            "hr" -> binding.languageCroatia.isChecked = true
            "bs" -> binding.languageBosnia.isChecked = true
            "de" -> binding.languageGerman.isChecked = true
            "tr" -> binding.languageTurki.isChecked = true
            "el" -> binding.languageGreek.isChecked = true
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