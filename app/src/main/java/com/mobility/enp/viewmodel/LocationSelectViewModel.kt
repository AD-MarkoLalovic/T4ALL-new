package com.mobility.enp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobility.enp.MyApplication
import com.mobility.enp.data.model.registration.RegistrationCountry
import com.mobility.enp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationSelectViewModel(private val repository: AuthRepository) : ViewModel() {

    val countries: List<RegistrationCountry> = repository.getCountries()

    private val _selectedCode = MutableStateFlow("RS")
    val selectCode = _selectedCode.asStateFlow()

    fun onCountrySelected(code: String) {
        _selectedCode.value = code
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepository = (this[APPLICATION_KEY] as MyApplication).repositoryAuth
                LocationSelectViewModel(
                    repository = myRepository
                )
            }
        }
    }
}