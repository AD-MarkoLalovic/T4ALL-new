package com.mobility.enp.view.dialogs

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.mobility.enp.R
import com.mobility.enp.databinding.LanguageDialogLayoutBinding
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.LanguageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.graphics.drawable.toDrawable

class LanguageDialog(private val onLanguageSelected: (String, Boolean) -> Unit) : DialogFragment() {

    private var _binding: LanguageDialogLayoutBinding? = null
    private val binding: LanguageDialogLayoutBinding get() = _binding!!
    private val viewModel: LanguageViewModel by viewModels { LanguageViewModel.Factory }
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }

    private var previousLanguageCode: String? = null

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

        setFranchise()
        observeViewModel()
        viewModel.fetchAllowedLanguages()

        binding.buttonCloseDialog.setOnClickListener { dismiss() }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
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
            languageCode?.let { viewModel.saveLanguage(it) }
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

    private fun observeViewModel() {
        viewModel.languages.observe(viewLifecycleOwner) { userLanguage ->
            Log.d("MARKO", "observeViewModel: $userLanguage ")
            userLanguage?.let {
                Log.d("MARKO 1", "observeViewModel: $userLanguage ")
                previousLanguageCode = it.userLanguage // Inicijalizujemo prethodni jezik
                updateSelectedLanguage(it.userLanguage)
            } ?: run {

                val shredPref = requireContext().getSharedPreferences("IntroLanguage", Context.MODE_PRIVATE)
                val langPick = shredPref.getString("selected_Language", "sr")
                Log.d("MARKO 2", "observeViewModel: $langPick ")
                previousLanguageCode = langPick
                updateSelectedLanguage(langPick)
            }

        }


        viewModel.selectedLanguage.observe(viewLifecycleOwner) { languageCode ->
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                if (languageCode != null && languageCode != previousLanguageCode) {
                    previousLanguageCode = languageCode
                    delay(500L)
                    dismiss()
                    onLanguageSelected(languageCode, true)
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.language_changed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }

    private fun updateSelectedLanguage(languageCode: String?) {
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