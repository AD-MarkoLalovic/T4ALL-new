package com.mobility.enp.view.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.mobility.enp.R
import com.mobility.enp.databinding.LanguageDialogLayoutBinding
import com.mobility.enp.viewmodel.LanguageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LanguageDialog(private val onLanguageSelected: (String, Boolean) -> Unit) : DialogFragment() {

    private var _binding: LanguageDialogLayoutBinding? = null
    private val binding: LanguageDialogLayoutBinding get() = _binding!!
    private val viewModel: LanguageViewModel by viewModels { LanguageViewModel.Factory }

    private var previousLanguageCode: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding = LanguageDialogLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        viewModel.fetchAllowedLanguages()

        binding.buttonCloseDialog.setOnClickListener { dismiss() }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val languageCode = when (checkedId) {
                R.id.languageEnglish -> "en"
                R.id.languageSerbian -> "cyr"
                R.id.languageSerbianLatin -> "sr"
                R.id.languageMontenegro -> "ar"
                R.id.languageMacedonian -> "mk"
                R.id.languageBosnia -> "bs"
                R.id.languageCroatia -> "hr"
                R.id.languageGerman -> "de"
                R.id.languageTurki -> "tr"
                R.id.languageGreek -> "el"
                else -> null
            }
            languageCode?.let { viewModel.saveLanguage(it) }
        }


    }

    private fun observeViewModel() {
        viewModel.languages.observe(viewLifecycleOwner) { userLanguage ->
            userLanguage?.let {
                previousLanguageCode = it.userLanguage // Inicijalizujemo prethodni jezik
                updateSelectedLanguage(it.userLanguage)
            }
        }


        viewModel.selectedLanguage.observe(viewLifecycleOwner) { languageCode ->
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                if (languageCode != null && languageCode != previousLanguageCode) {
                    previousLanguageCode = languageCode
                    delay(500L)
                    dismiss()
                    onLanguageSelected(languageCode, true)
                    Toast.makeText(requireContext(), getString(R.string.language_changed), Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun updateSelectedLanguage(languageCode: String?) {
        when (languageCode) {
            "cyr" -> binding.languageSerbian.isChecked = true
            "sr" -> binding.languageSerbianLatin.isChecked = true
            "en" -> binding.languageEnglish.isChecked = true
            "ar" -> binding.languageMontenegro.isChecked = true
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