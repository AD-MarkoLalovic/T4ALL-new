package com.mobility.enp.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_room_models.FcmToken
import com.mobility.enp.data.model.api_room_models.UserLoginResponseRoomTable
import com.mobility.enp.data.model.login.LoginBody
import com.mobility.enp.data.model.login.UserResponse
import com.mobility.enp.data.room.LastUser
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.network.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private var database: DRoom? = null
    private val _lastUserEmail: MutableLiveData<LastUser> = MutableLiveData()
    val lastUserEmail: LiveData<LastUser> get() = _lastUserEmail

    var loginLiveData = MutableLiveData<UserResponse>()

    fun initDatabase() {
        database = DRoom.getRoomInstance(getApplication())
    }

    fun getLastUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val lastUser = database?.lastUserDao()?.getLastUser()
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
        database?.loginDao()?.deleteAll()
        database?.loginDao()?.insert(userLoginResponseRoomTable)
        database?.loginDao()?.fetchAllowedUsers()?.let { user ->
            database?.fcmToken()?.getTableData().let { fcmToken ->
                fcmToken?.let {
                    Repository.postFcmToken(it, user.accessToken, errorBody)
                }
            }
        }
    }

    suspend fun storeLastUserEmail(email: String) {
        database?.lastUserDao()?.deleteLastUser()
        database?.lastUserDao()?.upsertLastUser(LastUser(email))
    }

    suspend fun getUserToken(): UserLoginResponseRoomTable? {
        return withContext(Dispatchers.IO) {
            database?.loginDao()?.fetchAllowedUsers()
        }
    }

    suspend fun writeFcmToken(token: String) {
        database?.fcmToken()?.deleteTable()
        database?.fcmToken()?.insertData(FcmToken(token))
    }

    suspend fun getLanguageKey(): String {
        val lang = Repository.getUserLanguage(getApplication())
        return "RS/$lang"
    }


}