package com.mobility.enp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobility.enp.MyApplication
import com.mobility.enp.data.repository.AuthRepository

class SplashAndIntroScreensViewModel(private val repository: AuthRepository) : ViewModel() {


    suspend fun fetchUserToken(): String? {
        return repository.getToken()?.accessToken
    }


    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepo = (this[APPLICATION_KEY] as MyApplication).repositoryAuth
                SplashAndIntroScreensViewModel(repository = myRepo)
            }
        }
    }
}