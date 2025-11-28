package com.mobility.enp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobility.enp.MyApplication
import com.mobility.enp.data.model.api_my_profile.ChangePasswordRequest
import com.mobility.enp.data.repository.AuthRepository
import com.mobility.enp.util.SubmitResultFold
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChangePasswordViewModel(private val repo: AuthRepository) : ViewModel() {

    private val _changePassword = MutableStateFlow<SubmitResultFold<Unit>>(SubmitResultFold.Idle)
    val changePassword: StateFlow<SubmitResultFold<Unit>> get() = _changePassword

    fun passwordChange(body: ChangePasswordRequest) {
        viewModelScope.launch {
            _changePassword.value = SubmitResultFold.Loading

            val result = repo.passwordChange(body)
            result.fold(
                onSuccess = {
                    _changePassword.value = SubmitResultFold.Success(Unit)
                },
                onFailure = { error ->
                    _changePassword.value = SubmitResultFold.Failure(error)
                }
            )
        }
    }

    fun resetChangePasswordState() {
        _changePassword.value = SubmitResultFold.Idle
    }

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepo = (this[APPLICATION_KEY] as MyApplication).repositoryAuth
                ChangePasswordViewModel(repo = myRepo)
            }
        }
    }

}