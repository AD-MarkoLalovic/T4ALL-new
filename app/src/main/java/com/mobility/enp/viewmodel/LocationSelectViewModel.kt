package com.mobility.enp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobility.enp.MyApplication
import com.mobility.enp.data.model.registration.RegistrationCountry
import com.mobility.enp.data.repository.AuthRepository

class LocationSelectViewModel(private val repository: AuthRepository) : ViewModel() {

    fun fetchCountries(context: Context): List<RegistrationCountry> {
        return repository.getCountries(context)
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