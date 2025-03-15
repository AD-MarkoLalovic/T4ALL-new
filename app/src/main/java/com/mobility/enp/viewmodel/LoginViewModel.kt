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
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_room_models.FcmToken
import com.mobility.enp.data.model.api_room_models.UserLoginResponseRoomTable
import com.mobility.enp.data.model.login.LoginBody
import com.mobility.enp.data.model.login.UserResponse
import com.mobility.enp.data.repository.LoginRepository
import com.mobility.enp.data.room.LastUser
import com.mobility.enp.network.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(private val repository: LoginRepository) : ViewModel() {
    private val _lastUserEmail: MutableLiveData<LastUser> = MutableLiveData()
    val lastUserEmail: LiveData<LastUser> get() = _lastUserEmail

    var loginLiveData = MutableLiveData<UserResponse>()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepository = (this[APPLICATION_KEY] as MyApplication).loginRepository
                LoginViewModel(
                    repository = myRepository
                )
            }
        }
    }

    fun getLastUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val lastUser = repository.getRoomDatabase()?.lastUserDao()?.getLastUser()
            lastUser?.let {
                _lastUserEmail.postValue(it)
            }
        }
    }

    suspend fun loginUser(
        context: Context,
        loginBody: LoginBody,
        errorBody: MutableLiveData<ErrorBody>
    ) {
        Repository.loginUser(loginLiveData, context, loginBody, errorBody)
    }

    suspend fun insertLoginToken(
        userLoginResponseRoomTable: UserLoginResponseRoomTable,
        errorBody: MutableLiveData<ErrorBody>
    ) {
        repository.getRoomDatabase()?.loginDao()?.deleteAll()
        repository.getRoomDatabase()?.loginDao()?.insert(userLoginResponseRoomTable)
        repository.getRoomDatabase()?.loginDao()?.fetchAllowedUsers()?.let { user ->
            repository.getRoomDatabase()?.fcmToken()?.getTableData().let { fcmToken ->
                fcmToken?.let {
                    Repository.postFcmToken(it, user.accessToken, errorBody)
                }
            }
        }
    }

    suspend fun storeLastUserEmail(email: String) {
        repository.getRoomDatabase()?.lastUserDao()?.deleteLastUser()
        repository.getRoomDatabase()?.lastUserDao()?.upsertLastUser(LastUser(email))
    }

    suspend fun getUserToken(): UserLoginResponseRoomTable? {
        return withContext(Dispatchers.IO) {
            repository.getRoomDatabase()?.loginDao()?.fetchAllowedUsers()
        }
    }

    suspend fun writeFcmToken(token: String) {
        repository.getRoomDatabase()?.fcmToken()?.deleteTable()
        repository.getRoomDatabase()?.fcmToken()?.insertData(FcmToken(token))
    }

    suspend fun getLanguageKey(context: Context): String {
        val lang = Repository.getUserLanguage(context)
        return "RS/$lang"
    }

    fun sendLanguage(context: Context) {
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

}