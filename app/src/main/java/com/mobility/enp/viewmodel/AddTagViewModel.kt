package com.mobility.enp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_tags.LostTagResponse
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.network.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddTagViewModel(application: Application) : AndroidViewModel(application) {
    private var database: DRoom? = DRoom.getRoomInstance(application)

    fun addTagForUser(
        serial: String,
        verificationCode: String,
        data: MutableLiveData<LostTagResponse>,
        errorBody: MutableLiveData<ErrorBody>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            database?.loginDao()?.fetchAllowedUsers()?.accessToken?.let {
                Repository.postAddTag(it, serial, verificationCode, errorBody, data, getApplication())
            }
        }
    }
}