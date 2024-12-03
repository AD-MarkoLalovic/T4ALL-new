package com.mobility.enp.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobility.enp.data.model.api_my_profile.UpdateUserInfoRequest
import com.mobility.enp.data.model.api_my_profile.basic_information.UserInfoData
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.network.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class BasicInformationViewModel(application: Application) : AndroidViewModel(application) {

    private val database: DRoom = DRoom.getRoomInstance(application)

    private val _saveChangesSuccess = MutableStateFlow(false)
    val saveChangesSuccess = _saveChangesSuccess.asStateFlow()

    private val _checkingInternet = MutableStateFlow(true)
    val checkingInternet = _checkingInternet.asStateFlow()

    private val _noData = MutableStateFlow(false)
    val noData = _noData.asStateFlow()

    private val _userInfoData = MutableStateFlow<UserInfoData?>(null)
    val userInfoData = _userInfoData.asStateFlow()

    private suspend fun getUserToken(): String? {
        return withContext(Dispatchers.IO) {
            database.loginDao().fetchAllowedUsers().accessToken
        }
    }

    fun isNetworkAvailable(): Boolean {
        return Repository.isNetworkAvailable(getApplication())
    }

    fun fetchPersonalInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            val networkAvailable = isNetworkAvailable()
            if (networkAvailable) {
                fetchFromNetwork()
            } else {
                fetchLocalData()
            }
        }
    }

    private suspend fun fetchFromNetwork() {
        try {
            val userToken = getUserToken()
            if (userToken != null) {
                val response = Repository.getUserPersonalInfo(userToken)
                _userInfoData.value = response.data

                database.basicInformationDao().deleteBasicInfoData()
                database.basicInformationDao().insertBasicInfoData(response.data!!)

                _checkingInternet.value = true
                _noData.value = false
            } else {
                _userInfoData.value = null
            }
        } catch (e: HttpException) {
            Log.d("BasicInformationViewModel", "fetchFromNetwork: HttpException ${e.message}")
        } catch (e: IOException) {
            Log.d("BasicInformationViewModel", "fetchFromNetwork: Network error ${e.message}")
        } catch (e: Exception) {
            Log.d("BasicInformationViewModel", "fetchFromNetwork: Unknown error ${e.message}")
        }
    }

    private suspend fun fetchLocalData() {
        withContext(Dispatchers.IO) {
            database.basicInformationDao().fetchBasicInformationData()
                .collectLatest { userInfo ->
                    if (userInfo != null) {
                        _userInfoData.value = userInfo
                        _checkingInternet.value = false
                        _noData.value = false
                    } else {
                        _noData.value = true
                    }
                }
        }
    }

    fun saveChanges(
        updatedInfo: UpdateUserInfoRequest,
        context: Context
    ) {
        _saveChangesSuccess.value = false
        if (isNetworkAvailable()) {
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
        } else {
            _checkingInternet.value = false
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

}
