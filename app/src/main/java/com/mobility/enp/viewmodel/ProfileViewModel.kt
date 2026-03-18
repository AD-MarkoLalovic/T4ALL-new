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
import com.google.gson.Gson
import com.mobility.enp.BuildConfig
import com.mobility.enp.MyApplication
import com.mobility.enp.data.model.TagOrderInputs
import com.mobility.enp.data.repository.ProfileRepository
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepository = (this[APPLICATION_KEY] as MyApplication).profileRepository
                ProfileViewModel(
                    repository = myRepository
                )
            }
        }
    }

    private val _postDeleteFcmToken =
        MutableStateFlow<SubmitResult<Boolean>>(SubmitResult.Empty)
    val postDeleteFcmToken: StateFlow<SubmitResult<Boolean>> get() = _postDeleteFcmToken

    private val _postLogoutUser =
        MutableStateFlow<SubmitResult<Boolean>>(SubmitResult.Empty)
    val postLogoutUser: StateFlow<SubmitResult<Boolean>> get() = _postLogoutUser

    private val _userInfo = MutableLiveData<String?>()
    val userInfo: LiveData<String?> get() = _userInfo

    private val _checkNet = MutableLiveData<Boolean?>()
    val checkNet: LiveData<Boolean?> get() = _checkNet

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> get() = _name

    private val _deletePic = MutableLiveData<Boolean>()
    val deletePic: LiveData<Boolean> get() = _deletePic

    private val _showRefundCard = MutableLiveData<Boolean>()
    val showRefundCard: LiveData<Boolean> get() = _showRefundCard

    private var _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _displayName = MutableLiveData<String>()
    val displayName: LiveData<String> = _displayName

    private suspend fun getUserToken(): String? {
        return withContext(Dispatchers.IO) {
            repository.userToken()
        }
    }

    suspend fun logout() {
        repository.deleteDatabase()
    }

    fun setRefundRequestVisibility() {
        if (isNetworkAvailable()) {
            _isLoading.value = true
            viewModelScope.launch {
                try {
                    val result = repository.getBasicUserInformation()
                    result.fold(onSuccess = { body ->
                        _displayName.value = body.data.displayName
                        val userCountry = body.data.country.code
                        val userType = body.data.customerType.type
                        val isFranchiser = body.data.isFranchiser

                        // Postavljanje vrednosti za _showRefundCard

                        _showRefundCard.value =
                            ((userCountry == "RS" || userType == 3) && !isFranchiser)
                    }, onFailure = {
                        _showRefundCard.value = false
                        _isLoading.value = false
                    })
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Error fetching user data", e)
                    _showRefundCard.value = false
                } finally {
                    _isLoading.value = false
                }
            }
        } else {
            _checkNet.value = false
            _showRefundCard.value = false
        }
    }

    suspend fun deleteFirebaseToken() {
        _postDeleteFcmToken.value = SubmitResult.Loading
        val fcmToken = repository.getFcmData()

        fcmToken?.fcm_token?.let { token ->
            var result = repository.deleteFirebaseToken(token)
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data == null) {
                    _postDeleteFcmToken.value = SubmitResult.Empty
                } else {
                    _postDeleteFcmToken.value =
                        SubmitResult.Success(data)
                }
            } else {
                when (val error = result.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        _postDeleteFcmToken.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _postDeleteFcmToken.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        _postDeleteFcmToken.value =
                            SubmitResult.FailureApiError(error.errorResponse.message ?: "")
                    }

                    else -> {}
                }
            }
        }

    }


    suspend fun postLogoutUser(
    ) {
        _postLogoutUser.value = SubmitResult.Loading
        val result = repository.logoutUser()
        if (result.isSuccess) {
            val data = result.getOrNull()
            if (data == null) {
                _postLogoutUser.value = SubmitResult.Empty
            } else {
                _postLogoutUser.value =
                    SubmitResult.Success(data)
            }
        } else {
            when (val error = result.exceptionOrNull()) {
                is NetworkError.ServerError -> {
                    _postLogoutUser.value = SubmitResult.FailureServerError
                }

                is NetworkError.NoConnection -> {
                    _postLogoutUser.value = SubmitResult.FailureNoConnection
                }

                is NetworkError.ApiError -> {
                    _postLogoutUser.value =
                        SubmitResult.FailureApiError(error.errorResponse.message ?: "")
                }

                else -> {}
            }
        }
    }

    fun deleteProfilePicture() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteProfilePicture()
            _deletePic.postValue(true)
        }
    }

    suspend fun checkStoredImageData(): Boolean {
        val list = repository.getStoredImage() ?: emptyList()
        return list.isNotEmpty()
    }

    fun fetchLocalData() {  // terms and conditions button
        if (isNetworkAvailable()) {
            viewModelScope.launch {
                try {
                    val result = repository.getBasicUserInformation()
                    result.fold(onSuccess = { body ->
                        val countryCode = body.data.country.code
                        val language = repository.getLanguageKey()
                        _userInfo.value = "$countryCode/$language"
                    }, onFailure = {
                        val error = result.exceptionOrNull()
                        Log.e("ProfileViewModel", error?.message ?: "Unknown error")
                    })
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Error fetching user data", e)
                }
            }
        } else {
            _checkNet.value = false
        }

    }

    fun resetUserInfo() {
        _userInfo.value = null
    }

    fun resetCheckNet() {
        _checkNet.value = null
    }

    fun isNetworkAvailable(): Boolean {
        return repository.isNetworkAvail()
    }

    suspend fun buildTagOrderUrl(): String = withContext(Dispatchers.IO) {
        val user = repository.getLocalBasicInfo()

        if (user != null) {
            val isCompany = user.customerType == 2
            val inputs = TagOrderInputs(
                customerType = user.customerType,
                city = user.city,
                postalCode = user.postalCode,
                email = user.email,
                phone = user.phone,
                firstName = user.firstName.takeIf { !isCompany },
                lastName = user.lastName.takeIf { !isCompany },
                companyName = user.companyName.takeIf { isCompany },
                mb = user.mb.takeIf { isCompany },
                pib = user.pib.takeIf { isCompany }
            )

            val json = Gson().toJson(inputs)
            BuildConfig.TAG_ORDER_BASE_URL
                .toHttpUrl()
                .newBuilder()
                .addQueryParameter("inputs", json)
                .build()
                .toString()
        } else {
            BuildConfig.TAG_ORDER_BASE_URL
        }
    }

    fun fetchCountryCode(): String? = repository.getCountryCode()
    fun fetchIsFranchiser(): Boolean = repository.getIsFranchiser()

}