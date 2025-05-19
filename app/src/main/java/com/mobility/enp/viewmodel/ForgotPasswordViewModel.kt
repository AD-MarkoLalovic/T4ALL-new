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
import com.mobility.enp.data.model.login.ForgotPasswordRequest
import com.mobility.enp.data.repository.AuthRepository
import com.mobility.enp.util.SubmitResultCustomerSupport
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(private val repository: AuthRepository) : ViewModel() {

    private var _submitSuccessful = MutableLiveData<SubmitResultCustomerSupport>()
    val submitSuccessful: LiveData<SubmitResultCustomerSupport> get() = _submitSuccessful

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


    fun forgotPass(email: ForgotPasswordRequest) {
        if (repository.netStateAvailable()) {
            viewModelScope.launch {
                try {
                    val result = repository.postForgotPassword(email)

                    if (result.isSuccess) {
                        _submitSuccessful.value = SubmitResultCustomerSupport.Success
                    } else {
                        _submitSuccessful.value = SubmitResultCustomerSupport.Failure
                    }
                } catch (e: Exception) {
                    _submitSuccessful.value = SubmitResultCustomerSupport.Failure
                }
            }
        } else {
            _submitSuccessful.value = SubmitResultCustomerSupport.NoNetwork
        }
    }
}