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
            val result = repository.sendLangKey()
            result.fold(
                onSuccess = {
                    Log.d(TAG, "language change was sent")
                },
                onFailure = { error ->
                    Log.d(TAG, "sendLanguage error occurred : $error")
                }
            )
        }
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