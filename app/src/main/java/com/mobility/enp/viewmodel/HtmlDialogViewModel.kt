package com.mobility.enp.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobility.enp.MyApplication
import com.mobility.enp.data.repository.CardRepository
import com.mobility.enp.util.AssetHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HtmlDialogViewModel(private val repository: CardRepository) : ViewModel() {

    private val _filePath: MutableLiveData<String> = MutableLiveData()
    val filepath: LiveData<String> get() = _filePath

    fun processContent(countryCode: String, documentType: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val folderPath = when (countryCode) {
                "ME" -> "montenegro"
                "MK" -> "north_macedonia"
                else -> throw IllegalArgumentException("Invalid country code: $countryCode")
            }

            val policyType = when (documentType) {
                "termsAndConditions" -> "general_terms"
                "privacyPolicy" -> "privacy_policy"
                else -> throw IllegalArgumentException("Invalid document type: $documentType")
            }

            val combined = "$folderPath/$policyType"
            Log.d(TAG, "processContent: $combined")
            val list = AssetHelper.getFileNames(context, combined)
            Log.d(TAG, "processContent: $list")

            val key = repository.getLangForCard()

            key?.let {
                val file = list.filter { s -> s.contains("$it.html", true) }
                if (file.isNotEmpty()) {
                    Log.d(TAG, "final list: ${file.toString()}")
                    _filePath.postValue(file[0])
                } else {
                    Log.d(TAG, "final document key: $key list $list")
                }
            } ?: run {
                throw IllegalStateException("Language key is null")
            }
        }
    }


    //cyr  - serbian cyrilic
    //sr - serbian latin
    //en -english
    //cnr - crnogorski
    //mk - makedonski
    //bs - bosansi
    //hr - hrvatski
    //de - nemacki
    //tr - turski
    //el - grcki

    companion object {
        const val TAG = "HTML_DIALOG"
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepository = (this[APPLICATION_KEY] as MyApplication).repositoryCard
                HtmlDialogViewModel(
                    repository = myRepository
                )
            }
        }
    }

}