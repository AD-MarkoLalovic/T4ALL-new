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
import com.mobility.enp.data.model.api_home_page.HomePageFcmTokenResponse
import com.mobility.enp.data.model.api_room_models.UserLoginResponseRoomTable
import com.mobility.enp.data.model.login.LoginBody
import com.mobility.enp.data.model.login.UserResponse
import com.mobility.enp.data.repository.AuthRepository
import com.mobility.enp.data.room.LastUser
import com.mobility.enp.network.Repository
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.viewmodel.UserPassViewModel.Companion.TAG
import com.mobility.enp.viewmodel.UserPassViewModel.Companion.TOKEN
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _fcmResponse =
        MutableStateFlow<SubmitResult<HomePageFcmTokenResponse>>(SubmitResult.Empty)
    val fcmToken: StateFlow<SubmitResult<HomePageFcmTokenResponse>> get() = _fcmResponse

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> get() = _loginState.asStateFlow()

    private val _lastUserEmail: MutableLiveData<LastUser> = MutableLiveData()
    val lastUserEmail: LiveData<LastUser> get() = _lastUserEmail

    fun getLastUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val lastUser = repository.getLastUser()
            lastUser?.let {
                _lastUserEmail.postValue(it)
            }
        }
    }

    fun loginUser(user: LoginBody, context: Context) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            val result = repository.loginUser(user)

            result.fold(
                onSuccess = { response ->
                    insertLoginToken(
                        UserLoginResponseRoomTable(
                            null,
                            response.data?.accessToken,
                            response.data?.tokenType,
                            user.email, user.password, response.data?.portal_key
                        )
                    )
                    storeLastUserEmail(user.email!!)
                    sendLanguage(context)

                    _loginState.value = LoginState.Success(response)
                },
                onFailure = { error ->
                    _loginState.value = LoginState.Failure(error)
                }
            )
        }
    }

    fun setIdleState() {
        _loginState.value = LoginState.Idle
    }

    private suspend fun insertLoginToken(
        userLoginResponseRoomTable: UserLoginResponseRoomTable,
    ) {
        repository.insertLoginToken(userLoginResponseRoomTable)
        repository.getUserFcmData().let { pair ->
            _fcmResponse.value = SubmitResult.Loading
            val result = repository.postFcmToken(pair.second, pair.first)
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data == null) {
                    _fcmResponse.value = SubmitResult.Empty
                } else {
                    _fcmResponse.value = SubmitResult.Success(data)
                }
            } else {
                when (val error = result.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.d(TAG, "Error while fetching tag serial data")
                        _fcmResponse.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _fcmResponse.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(TOKEN, "invalid token detected login out user")
                                _fcmResponse.value =
                                    SubmitResult.InvalidApiToken(
                                        error.errorResponse.code ?: 0,
                                        error.errorResponse.message ?: ""
                                    )
                            }

                            else -> {
                                _fcmResponse.value =
                                    SubmitResult.FailureApiError(
                                        error.errorResponse.message ?: ""
                                    )
                                Log.d(TAG, "api error ${error.errorResponse.message}")
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private suspend fun storeLastUserEmail(email: String) {
        repository.storeLastUserEmail(email)
    }

    suspend fun getUserToken(): UserLoginResponseRoomTable? {
        return repository.getStoredUser()
    }

    fun writeFcmToken(token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.writeFcmToken(token)
        }
    }

    suspend fun getLanguageKey(): String {
        val lang = repository.getLanguageKey()
        return "RS/$lang"
    }

    private fun sendLanguage(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = getUserToken()
                token?.let {
                    Repository.sendLanguageKey(it.accessToken, context)
                    Log.d("LoginViewModel", "Language send successfully")
                } ?: run {
                    Log.e("LoginViewModel", "Token is null")
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error sending language: ${e.message}", e)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepository = (this[APPLICATION_KEY] as MyApplication).repositoryAuth
                LoginViewModel(
                    repository = myRepository
                )
            }
        }
    }

}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val userResponse: UserResponse) : LoginState()
    data class Failure(val error: Throwable) : LoginState()
}