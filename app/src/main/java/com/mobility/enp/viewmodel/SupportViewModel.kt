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
import com.mobility.enp.data.model.api_my_profile.SupportRequest
import com.mobility.enp.data.model.deactivation.DeactivateAccountModel
import com.mobility.enp.data.repository.ProfileRepository
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SupportViewModel(private val repository: ProfileRepository) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepository = (this[APPLICATION_KEY] as MyApplication).profileRepository
                SupportViewModel(
                    repository = myRepository
                )
            }
        }
    }


    private val _supportMessageSentLiveData =
        MutableStateFlow<SubmitResult<Boolean>>(SubmitResult.Empty)
    val supportMessageSentLiveData: StateFlow<SubmitResult<Boolean>>
        get() = _supportMessageSentLiveData

    private val _checkNetSendSupport = MutableLiveData<Boolean>()
    val checkNetSendSupport: LiveData<Boolean> get() = _checkNetSendSupport

    private val _deactivateAccount = MutableStateFlow<SubmitResult<DeactivateAccountModel>>(
        SubmitResult.Empty
    )
    val deactivateAccount: StateFlow<SubmitResult<DeactivateAccountModel>> get() = _deactivateAccount


    fun sendSupportMessage(message: String) {
        if (repository.isNetworkAvail()) {
            _supportMessageSentLiveData.value = SubmitResult.Loading
            viewModelScope.launch {
                val result = repository.sendSupportMessage(SupportRequest(message))
                if (result.isSuccess) {
                    val data = result.getOrNull()
                    if (data == null) {
                        _supportMessageSentLiveData.value = SubmitResult.Empty
                    } else {
                        _supportMessageSentLiveData.value =
                            SubmitResult.Success(data)
                    }
                } else {
                    when (val error = result.exceptionOrNull()) {
                        is NetworkError.ServerError -> {
                            _supportMessageSentLiveData.value = SubmitResult.FailureServerError
                        }

                        is NetworkError.NoConnection -> {
                            _supportMessageSentLiveData.value = SubmitResult.FailureNoConnection
                        }

                        is NetworkError.ApiError -> {
                            _supportMessageSentLiveData.value =
                                SubmitResult.FailureApiError(error.errorResponse.message ?: "")
                        }

                        else -> {}
                    }
                }
            }
        } else {
            _checkNetSendSupport.postValue(false)
        }
    }

    fun sendDeactivationRequest(
        pair: Pair<String, String>,
    ) {
        if (repository.isNetworkAvail()) {
            viewModelScope.launch(Dispatchers.IO) {
                val result = repository.postDeactivateAccount(pair)

                if (result.isSuccess) {
                    val data = result.getOrNull()
                    if (data == null) {
                        _deactivateAccount.value = SubmitResult.Empty
                    } else {
                        _deactivateAccount.value =
                            SubmitResult.Success(data)
                    }
                } else {
                    when (val error = result.exceptionOrNull()) {
                        is NetworkError.ServerError -> {
                            _deactivateAccount.value = SubmitResult.FailureServerError
                        }

                        is NetworkError.NoConnection -> {
                            _deactivateAccount.value = SubmitResult.FailureNoConnection
                        }

                        is NetworkError.ApiError -> {
                            _deactivateAccount.value =
                                SubmitResult.FailureApiError(error.errorResponse.message ?: "")
                        }

                        else -> {}
                    }
                }
            }
        } else {
            _checkNetSendSupport.postValue(false)
        }
    }

}