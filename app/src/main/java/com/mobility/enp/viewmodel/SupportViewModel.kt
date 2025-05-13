package com.mobility.enp.viewmodel

import android.content.Context
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
import com.mobility.enp.data.model.api_my_profile.SupportRequest
import com.mobility.enp.data.model.deactivation.DeactivateAccountModel
import com.mobility.enp.data.repository.ProfileRepository
import com.mobility.enp.network.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SupportViewModel(private val repository: ProfileRepository) : ViewModel() {

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


    private val _supportMessageSentLiveData = MutableLiveData<Boolean>()
    val supportMessageSentLiveData: LiveData<Boolean>
        get() = _supportMessageSentLiveData

    private val _checkNetSendSupport = MutableLiveData<Boolean>()
    val checkNetSendSupport: LiveData<Boolean> get() = _checkNetSendSupport

    private val _deactivateAccount = MutableLiveData<DeactivateAccountModel>()
    val deactivateAccount: LiveData<DeactivateAccountModel> get() = _deactivateAccount


    fun sendSupportMessage(message: String, error: MutableLiveData<ErrorBody>) {
        if (repository.isNetworkAvail()) {
            viewModelScope.launch {
                try {
                    val userToken = repository.userToken()
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

    fun sendDeactivationRequest(pair: Pair<String, String>, error: MutableLiveData<ErrorBody>,context: Context) {
        if (repository.isNetworkAvail()) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val userToken = repository.userToken()
                    userToken.let {
                        Repository.postDeactivateAccount(
                            pair, error, _deactivateAccount, it, context
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