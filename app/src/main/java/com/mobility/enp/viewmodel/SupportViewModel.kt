package com.mobility.enp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_my_profile.SupportRequest
import com.mobility.enp.data.model.deactivation.DeactivateAccountModel
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.network.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SupportViewModel(application: Application) : AndroidViewModel(application) {

    private val database: DRoom = DRoom.getRoomInstance(application)

    private val _supportMessageSentLiveData = MutableLiveData<Boolean>()
    val supportMessageSentLiveData: LiveData<Boolean>
        get() = _supportMessageSentLiveData

    private val _checkNetSendSupport = MutableLiveData<Boolean>()
    val checkNetSendSupport: LiveData<Boolean> get() = _checkNetSendSupport

    private val _deactivateAccount = MutableLiveData<DeactivateAccountModel>()
    val deactivateAccount: LiveData<DeactivateAccountModel> get() = _deactivateAccount

    private suspend fun getUserToken(): String? {
        return withContext(Dispatchers.IO) {
            database.loginDao().fetchAllowedUsers().accessToken
        }
    }

    private fun isNetworkAvailable(): Boolean {
        return Repository.isNetworkAvailable(getApplication())
    }

    fun sendSupportMessage(message: String, error: MutableLiveData<ErrorBody>) {
        if (isNetworkAvailable()) {
            viewModelScope.launch {
                try {
                    val userToken = getUserToken()
                    userToken.let { token ->
                        val sendMessage = SupportRequest(message)
                        Repository.sendSupportMessage(sendMessage, token, error)
                        _supportMessageSentLiveData.postValue(true)
                    }

                } catch (e: Exception) {
                    _supportMessageSentLiveData.postValue(false)
                }
            }
        } else {
            _checkNetSendSupport.postValue(false)
        }
    }

    fun sendDeactivationRequest(pair: Pair<String, String>, error: MutableLiveData<ErrorBody>) {
        if (isNetworkAvailable()) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val userToken = getUserToken()
                    userToken.let {
                        Repository.postDeactivateAccount(
                            pair, error, _deactivateAccount, it, getApplication()
                        )
                    }

                } catch (e: Exception) {
                    _supportMessageSentLiveData.postValue(false)
                }
            }
        } else {
            _checkNetSendSupport.postValue(false)
        }
    }

}