package com.mobility.enp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobility.enp.MyApplication
import com.mobility.enp.data.model.api_room_models.UserLanguage
import com.mobility.enp.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LanguageViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _languages = MutableLiveData<UserLanguage>()
    val languages: LiveData<UserLanguage> get() = _languages

    private val _selectedLanguage = MutableLiveData<String>()
    val selectedLanguage: LiveData<String> get() = _selectedLanguage

    fun fetchAllowedLanguages() {
        viewModelScope.launch {
            _languages.value = repository.getAllowedUserLanguage()
        }
    }

    fun saveLanguage(languageCode: String) {
        viewModelScope.launch {
            repository.clearLanguages()
            repository.saveLanguage(UserLanguage(1, languageCode))
            _selectedLanguage.value = languageCode
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepository = (this[APPLICATION_KEY] as MyApplication).repositoryAuth
                LanguageViewModel(
                    repository = myRepository
                )
            }
        }
    }
}