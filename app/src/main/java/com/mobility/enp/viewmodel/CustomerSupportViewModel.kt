package com.mobility.enp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobility.enp.MyApplication
import com.mobility.enp.data.model.login.CustomerSupport
import com.mobility.enp.data.repository.AuthRepository
import com.mobility.enp.util.SubmitResultFold
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CustomerSupportViewModel(private val repo: AuthRepository) : ViewModel() {

    private var _submitSuccessful = MutableStateFlow<SubmitResultFold<Unit>>(SubmitResultFold.Idle)
    val submitSuccessful: StateFlow<SubmitResultFold<Unit>> get() = _submitSuccessful

    fun sendCustomerSupport(data: CustomerSupport) {
        viewModelScope.launch {
            _submitSuccessful.value = SubmitResultFold.Loading

            val result = repo.sendCustomerSupport(data)
            result.fold(
                onSuccess = {
                    _submitSuccessful.value = SubmitResultFold.Success(Unit)
                },
                onFailure = { error ->
                    _submitSuccessful.value = SubmitResultFold.Failure(error)
                }
            )
        }
    }

    fun resetSubmitState() {
        _submitSuccessful.value = SubmitResultFold.Idle
    }

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepo = (this[APPLICATION_KEY] as MyApplication).repositoryAuth
                CustomerSupportViewModel(repo = myRepo)
            }
        }
    }
}