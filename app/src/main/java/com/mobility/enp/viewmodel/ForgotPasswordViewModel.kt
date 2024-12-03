package com.mobility.enp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.login.ForgotPasswordRequest
import com.mobility.enp.network.Repository
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val _result = MutableLiveData<Boolean>()
    val result: LiveData<Boolean> get() = _result

    private val _checkNetForgotPass = MutableLiveData<Boolean>()
    val checkNetForgotPass: LiveData<Boolean> get() = _checkNetForgotPass

    private fun isNetworkAvailable(): Boolean {
        return Repository.isNetworkAvailable(getApplication())
    }

    fun forgotPass(email: ForgotPasswordRequest, errorBody: MutableLiveData<ErrorBody>) {
        if (isNetworkAvailable()) {
            viewModelScope.launch {
                Repository.postForgotPassword(
                    email,
                    errorBody,
                    _result
                )
            }
        } else {
            _checkNetForgotPass.postValue(false)
        }
    }
}