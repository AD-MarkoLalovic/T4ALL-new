package com.mobility.enp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_my_profile.ChangePasswordRequest
import com.mobility.enp.data.model.api_room_models.UserLoginResponseRoomTable
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.network.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChangePasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val database: DRoom = DRoom.getRoomInstance(application)

    private val _changePasswordStatusLiveData = MutableLiveData<Boolean>()
    val changePasswordStatusLiveData: LiveData<Boolean> get() = _changePasswordStatusLiveData

    private val _checkNetChangePass = MutableLiveData<Boolean>()
    val checkNetChangePass: LiveData<Boolean> get() = _checkNetChangePass


    private suspend fun getUserToken(): UserLoginResponseRoomTable {
        return withContext(Dispatchers.IO) {
            database.loginDao().fetchAllowedUsers()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        return Repository.isNetworkAvailable(getApplication())
    }

    suspend fun getUserPassword(): String? {
        val user = database.loginDao().fetchAllowedUsers()
        return user.password
    }

    fun changePassword(
        oldPassword: String,
        newPassword: String,
        errorBody: MutableLiveData<ErrorBody>
    ) {
        if (isNetworkAvailable()) {
            viewModelScope.launch {
                if (oldPassword.isBlank() || newPassword.isBlank()) {
                    _changePasswordStatusLiveData.postValue(false)
                } else {
                    val userToken = getUserToken()
                    userToken.let { token ->
                        val changePasswordRequest = ChangePasswordRequest(
                            oldPassword, newPassword, newPassword
                        )
                        Repository.changePassword(
                            changePasswordRequest,
                            token.accessToken,
                            getApplication(),
                            errorBody
                        )
                        _changePasswordStatusLiveData.postValue(true)
                    }
                }
            }
        } else {
            _checkNetChangePass.postValue(false)
        }
    }

}