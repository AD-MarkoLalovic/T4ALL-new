package com.mobility.enp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.network.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val database: DRoom = DRoom.getRoomInstance(application)

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

    private val homeDisplayName: String = Repository.getDisplayName(getApplication()).toString()
    val displayName: LiveData<String> =
        database.basicInfoDao().fetchDisplayName()
            .map { it.orEmpty().ifEmpty { homeDisplayName } }
            .asLiveData()


    private suspend fun getUserToken(): String? {
        return withContext(Dispatchers.IO) {
            database.loginDao().fetchAllowedUsers().accessToken
        }
    }

    fun logout() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.clearAllData()
            }
        }
    }

    fun setRefundRequestVisibility() {
        if (isNetworkAvailable()) {
            _isLoading.value = true
            viewModelScope.launch {
                try {
                    val token = getUserToken()
                    token?.let {
                        val userInfo = Repository.getUserPersonalInfo(it)
                        val userCountry = userInfo.data?.country?.code
                        val userType = userInfo.data?.customerType?.type
                        val isFranchiser = userInfo.data?.isFranchiser

                        // Postavljanje vrednosti za _showRefundCard
                        _showRefundCard.value =
                            ((userCountry == "RS" || userType == 3) && isFranchiser == false)
                    }
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

    suspend fun deleteFirebaseToken(errorBody: MutableLiveData<ErrorBody>) {
        val userToken = getUserToken()
        val fcmToken = withContext(Dispatchers.IO) {
            database.fcmToken().getTableData()
        }

        userToken?.let { token ->
            fcmToken.fcm_token?.let { fcmToken ->
                Repository.deleteFirebaseToken(token, fcmToken, errorBody)
            }
        }

    }

    suspend fun postLogoutUser(
        errorBody: MutableLiveData<ErrorBody>
    ) {
        val userToken = getUserToken()
        Repository.logoutUser(userToken, errorBody)
    }

    fun deleteProfilePicture() {
        viewModelScope.launch(Dispatchers.IO) {
            database.profileImageDao().deleteAll();
            _deletePic.postValue(true)
        }
    }

    suspend fun checkStoredImageData(): Boolean {
        val list = database.profileImageDao().selectAll()
        return list.isNotEmpty()
    }

    fun fetchLocalData() {
        if (isNetworkAvailable()) {
            viewModelScope.launch {
                try {
                    val token = getUserToken()

                    token?.let {
                        val countryCode = async {
                            val response = Repository.getCountryCode(it)
                            response.data?.country?.code
                        }

                        val userLanguage = async {
                            Repository.getUserLanguage(getApplication())
                        }

                        val code = countryCode.await()
                        val language = userLanguage.await()

                        _userInfo.value = "$code/$language"
                    }
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Error fetching local data", e)
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
        return Repository.isNetworkAvailable(getApplication())
    }

    fun fetchDisplayName(): String {
        return Repository.getDisplayName(getApplication()).toString()
    }

}