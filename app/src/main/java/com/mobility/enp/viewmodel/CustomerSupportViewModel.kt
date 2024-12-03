package com.mobility.enp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobility.enp.data.model.login.CustomerSupport
import com.mobility.enp.network.Repository
import com.mobility.enp.util.SubmitResultCustomerSupport
import kotlinx.coroutines.launch

class CustomerSupportViewModel(application: Application) : AndroidViewModel(application) {

    private var _submitSuccessful = MutableLiveData<SubmitResultCustomerSupport>()
    val submitSuccessful: LiveData<SubmitResultCustomerSupport> get() = _submitSuccessful

    fun submitCustomerSupport(subject: String, email: String, message: String) {
        val customerSupport = CustomerSupport(subject, email, message)

        viewModelScope.launch {
            _submitSuccessful.value = SubmitResultCustomerSupport.Loading

            if (Repository.isNetworkAvailable(getApplication())) {
                try {
                    val response = Repository.sendCustomerSupport(customerSupport)
                    if (response.isSuccessful) {
                        _submitSuccessful.value = SubmitResultCustomerSupport.Success
                    } else {
                        _submitSuccessful.value = SubmitResultCustomerSupport.Failure
                    }
                } catch (e: Exception) {
                    Log.d("CustomerSupportViewModel", "Error: $e")
                    _submitSuccessful.value = SubmitResultCustomerSupport.Failure
                }
            } else {
                Log.d("CustomerSupportViewModel", "No network available")
                _submitSuccessful.value = SubmitResultCustomerSupport.NoNetwork
            }
        }
    }
}