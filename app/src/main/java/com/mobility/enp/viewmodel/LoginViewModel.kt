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
import com.mobility.enp.data.model.api_home_page.HomePageFcmTokenResponse
import com.mobility.enp.data.model.api_room_models.UserLoginResponseRoomTable
import com.mobility.enp.data.model.login.LoginBody
import com.mobility.enp.data.model.login.UserResponse
import com.mobility.enp.data.repository.AuthRepository
import com.mobility.enp.data.room.LastUser
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.viewmodel.UserPassViewModel.Companion.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    fun loginUser(user: LoginBody) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            val result = repository.loginUser(user)

            result.fold(
                onSuccess = { response ->
                    val entity = UserLoginResponseRoomTable(
                        null,
                        response.data?.accessToken,
                        response.data?.tokenType,
                        user.email, user.password, response.data?.portal_key
                    )

                    withContext(Dispatchers.IO) {
                        repository.insertLoginToken(entity)
                        repository.storeLastUserEmail(user.email!!)
                    }

                    sendLanguage()

                    _loginState.value = LoginState.Success(response, response.data?.portal_key)
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

    private fun sendLanguage() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.sendLangKey()
            result.fold(
                onSuccess = {
                    Log.d(TAG, "language change was sent")
                },
                onFailure = { error ->
                    Log.d(TAG, "sendLanguage error occurred : $error")
                }
            )
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
    data class Success(val userResponse: UserResponse, val portalKey: String?) : LoginState()
    data class Failure(val error: Throwable) : LoginState()
}