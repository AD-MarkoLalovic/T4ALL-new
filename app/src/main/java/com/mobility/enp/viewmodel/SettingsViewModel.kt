package com.mobility.enp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobility.enp.MyApplication
import com.mobility.enp.data.repository.UserRepository

class SettingsViewModel(private val repository: UserRepository) : ViewModel() {

    suspend fun sendingLangToServer() {
        repository.sendLangKey()
    }

    companion object {
        const val TAG = "SETTINGS_FRAGMENT"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepository = (this[APPLICATION_KEY] as MyApplication).repositoryUser
                SettingsViewModel(
                    repository = myRepository
                )
            }
        }
    }
}