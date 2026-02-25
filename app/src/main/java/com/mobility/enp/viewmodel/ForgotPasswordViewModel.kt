package com.mobility.enp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobility.enp.MyApplication
import com.mobility.enp.data.model.login.ForgotPasswordRequest
import com.mobility.enp.data.repository.AuthRepository
import com.mobility.enp.util.ForgotPasswordUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(private val repository: AuthRepository) : ViewModel() {

    private var _submitSuccessful =
        MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val submitSuccessful = _submitSuccessful.asStateFlow()

    fun forgotPass(email: ForgotPasswordRequest) {
        viewModelScope.launch {
            _submitSuccessful.value = ForgotPasswordUiState.Loading

            val result = repository.postForgotPassword(email)
            result.fold(
                onSuccess = {
                    _submitSuccessful.value = ForgotPasswordUiState.Success
                },
                onFailure = { error ->
                    _submitSuccessful.value = ForgotPasswordUiState.Failure(error)
                }
            )

        }
    }

    fun clearState() {
        _submitSuccessful.value = ForgotPasswordUiState.Idle
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepository = (this[APPLICATION_KEY] as MyApplication).repositoryAuth
                ForgotPasswordViewModel(
                    repository = myRepository
                )
            }
        }
    }
}