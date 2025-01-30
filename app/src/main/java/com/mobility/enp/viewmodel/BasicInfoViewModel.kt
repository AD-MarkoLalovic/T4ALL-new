package com.mobility.enp.viewmodel

import android.content.Context
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
import com.mobility.enp.data.repository.UserRepository
import com.mobility.enp.network.Repository
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.view.ui_models.BasicInfoUIModel
import kotlinx.coroutines.launch

class BasicInfoViewModel(val repository: UserRepository) : ViewModel() {

    private val _basicInfoUI = MutableLiveData<SubmitResult<BasicInfoUIModel>>().apply {
        value = SubmitResult.Loading
    }
    val basicInfo: LiveData<SubmitResult<BasicInfoUIModel>> = _basicInfoUI

    private val _saveChangesSuccess = MutableLiveData<Boolean>().apply {
        value = false
    }
    val saveChangesSuccess: LiveData<Boolean> = _saveChangesSuccess

    init {
        fetchBasicInfo()
    }

    private fun fetchBasicInfo() {
        viewModelScope.launch {
            _basicInfoUI.postValue(SubmitResult.Loading)

            val localData = repository.getLocalBasicInfo()
            localData?.let {
                _basicInfoUI.postValue(SubmitResult.Success(localData.toUIModel()))
            }

            val result = repository.getBasicInfoFromServer()
            if (result.isSuccess) {
                val basicInfoEntity = result.getOrNull()

                if (basicInfoEntity == null) {
                    _basicInfoUI.postValue(SubmitResult.Empty)
                } else {
                    val uiModel = basicInfoEntity.toUIModel()
                    _basicInfoUI.postValue(SubmitResult.Success(uiModel))
                }
            } else {
                when (val error = result.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.e(
                            "BasicInfoViewModel",
                            "Greška tokom preuzimanja refund zahteva",
                            error
                        )
                        _basicInfoUI.postValue(SubmitResult.FailureServerError)
                    }

                    is NetworkError.NoConnection -> {
                        _basicInfoUI.postValue(SubmitResult.FailureNoConnection)
                    }
                    is NetworkError.ApiError -> {
                        _basicInfoUI.postValue(SubmitResult.FailureApiError(error.errorResponse.message!!))
                    }
                }
            }
        }
    }

    private suspend fun setDisplayName(
        userToken: String,
        context: Context,
    ) {
        val response = Repository.getUserPersonalInfo(userToken)
        val displayName = response.data?.displayName.toString()
        Repository.saveDisplayName(context, displayName)
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

   /* fun saveChanges(
        updatedInfo: UpdateUserInfoRequest,
        context: Context
    ) {
        _saveChangesSuccess.value = false

            viewModelScope.launch {
                try {
                    val userToken = getUserToken()
                    userToken?.let { token ->
                        Repository.updateUserInfo(
                            database.basicInformationDao(),
                            updatedInfo,
                            token
                        )
                        _saveChangesSuccess.value = true
                        setDisplayName(token, context)
                    }

                } catch (e: HttpException) {
                    Log.d("BasicInformationViewModel", "saveChanges: HttpException ${e.message}")
                } catch (e: IOException) {
                    Log.d("BasicInformationViewModel", "saveChanges: Network error ${e.message}")
                } catch (e: Exception) {
                    Log.d("BasicInformationViewModel", "saveChanges: Unknown error ${e.message}")
                }
            }

    }*/







