package com.mobility.enp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobility.enp.MyApplication
import com.mobility.enp.data.model.api_my_profile.basic_information.request.UpdateUserDataRequest
import com.mobility.enp.data.repository.UserRepository
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.view.ui_models.BasicInfoUIModel
import kotlinx.coroutines.launch

class BasicInfoViewModel(val repository: UserRepository) : ViewModel() {

    private val _basicInfoUI = MutableLiveData<SubmitResult<BasicInfoUIModel>>().apply {
        value = SubmitResult.Loading
    }
    val basicInfo: LiveData<SubmitResult<BasicInfoUIModel>> = _basicInfoUI

    private val _updateBasicInfoUI = MutableLiveData<SubmitResult<BasicInfoUIModel>>().apply {
        value = SubmitResult.Loading
    }
    val updateBasicInfoUI: LiveData<SubmitResult<BasicInfoUIModel>> = _updateBasicInfoUI

    init {
        fetchBasicInfo()
    }

    fun fetchBasicInfo() {
        viewModelScope.launch {
            _basicInfoUI.value = SubmitResult.Loading

            val localData = repository.getLocalBasicInfo()
            localData?.let {
                _basicInfoUI.value = SubmitResult.Success(localData.toUIModel())
            }

            val result = repository.getBasicInfoFromServer()
            if (result.isSuccess) {
                val basicInfoEntity = result.getOrNull()

                if (basicInfoEntity == null) {
                    _basicInfoUI.value = SubmitResult.Empty
                } else {
                    val uiModel = basicInfoEntity.toUIModel()
                    _basicInfoUI.value = SubmitResult.Success(uiModel)
                }
            } else {
                when (val error = result.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.e(
                            "BasicInfoViewModel",
                            "Greška tokom preuzimanja osnovnih podataka korisnika",
                            error
                        )
                        _basicInfoUI.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _basicInfoUI.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(
                                    "API_TOKEN BasicInfoViewModel",
                                    "invalid token detected login out user"
                                )
                                _basicInfoUI.value =
                                    SubmitResult.InvalidApiToken(
                                        error.errorResponse.code,
                                        error.errorResponse.message ?: ""
                                    )
                            }

                            else -> {
                                _basicInfoUI.value =
                                    SubmitResult.FailureApiError(
                                        error.errorResponse.message ?: ""
                                    )
                                Log.d(
                                    "API_TOKEN BasicInfoViewModel",
                                    "BasicInfoViewModel api error ${error.errorResponse.message}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun updateUserData(data: UpdateUserDataRequest) {
        viewModelScope.launch {
            _updateBasicInfoUI.value = SubmitResult.Loading

            val result = repository.putUpdateUserData(data)
            if (result.isSuccess) {
                val basicInfoEntity = result.getOrNull()

                if (basicInfoEntity == null) {
                    _updateBasicInfoUI.value = SubmitResult.Empty
                } else {
                    val uiModel = basicInfoEntity.toUIModel()
                    _updateBasicInfoUI.value = SubmitResult.Success(uiModel)
                }
            } else {
                when (val error = result.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.e(
                            "BasicInfoViewModel",
                            "Greška tokom azuriranja podataka korisnika",
                            error
                        )
                        _updateBasicInfoUI.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _updateBasicInfoUI.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(
                                    "API_TOKEN BasicInfoViewModel",
                                    "invalid token detected login out user"
                                )
                                _updateBasicInfoUI.value =
                                    SubmitResult.InvalidApiToken(
                                        error.errorResponse.code,
                                        error.errorResponse.message ?: ""
                                    )
                            }

                            else -> {
                                _updateBasicInfoUI.value =
                                    SubmitResult.FailureApiError(
                                        error.errorResponse.message ?: ""
                                    )
                                Log.d(
                                    "API_TOKEN BasicInfoViewModel",
                                    "BasicInfoViewModel api error ${error.errorResponse.message}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun existLocalData(): Boolean {
        val data = repository.getLocalBasicInfo()
        return data != null
    }

    fun isInternetAvailable(): Boolean {
        return repository.isNetAvailable()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepository = (this[APPLICATION_KEY] as MyApplication).repositoryUser
                BasicInfoViewModel(
                    repository = myRepository
                )
            }
        }
    }
}






