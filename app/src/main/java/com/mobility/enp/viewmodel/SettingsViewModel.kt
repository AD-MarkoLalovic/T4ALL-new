package com.mobility.enp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobility.enp.MyApplication
import com.mobility.enp.data.repository.UserRepository
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: UserRepository) : ViewModel() {

    fun sendingLangToServer() {
        viewModelScope.launch {
            try {
                repository.sendLangKey()
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error sending language: ${e.message}", e)
            }
        }
    }

    companion object {
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