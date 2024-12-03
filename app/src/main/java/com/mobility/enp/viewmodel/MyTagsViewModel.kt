package com.mobility.enp.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_tags.LostTagResponse
import com.mobility.enp.data.model.api_tags.PostLostTag
import com.mobility.enp.data.model.api_tags.TagStatus
import com.mobility.enp.data.model.api_tags.TagsResponse
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.network.Repository

class MyTagsViewModel(application: Application) : AndroidViewModel(application) {

    private val database: DRoom = DRoom.getRoomInstance(getApplication())

    lateinit var allTagsFiltered: List<TagStatus>
    private lateinit var lastBlockedTagSerial: String

    private val _tagApiData: MutableLiveData<TagsResponse> = MutableLiveData()
    val tagApiData: LiveData<TagsResponse> get() = _tagApiData
    private val _lostTag: MutableLiveData<LostTagResponse> = MutableLiveData()
    val lostTag: LiveData<LostTagResponse> get() = _lostTag
    private val _errorBody: MutableLiveData<ErrorBody> = MutableLiveData()
    val errorBody: LiveData<ErrorBody> get() = _errorBody
    private var _isInternetAvailable: MutableLiveData<Boolean> = MutableLiveData()
    val isInternetAvailable: LiveData<Boolean> get() = _isInternetAvailable

    suspend fun getTagsApiData(
        context: Context,
    ) {
        if (Repository.isNetworkAvailable(context)) {
            database.loginDao().fetchAllowedUsers().accessToken?.let { token ->
                Repository.getTags(token, 1, 1000, _errorBody, _tagApiData, getApplication())
            }
        } else {
            _isInternetAvailable.postValue(false)
        }

    }


    fun newFilterLogic(tagStatus: Int): TagsResponse? {  // status filter
        val fullList = tagApiData.value ?: return null

        if (tagStatus == -1) {  // any button is pressed
            return fullList
        }

        // Filter the tags based on whether any of their statuses contains the passed status
        val filteredList = fullList.data.tags.filter { tag ->
            tag.statuses.any { status ->
                status.status.value == tagStatus
            }
        }

        // Create a new instance of Data with the filtered list of tags
        val newData = fullList.data.copy(
            tags = filteredList
        )

        // Create a new instance of TagsResponse with the new data
        return fullList.copy(
            data = newData
        )

    }

    fun newFilterLogic(tagSerial: String): TagsResponse? {  // serial filter

        val fullList = tagApiData.value

        if (tagSerial.isEmpty()) {
            return fullList
        }

        fullList?.let { tagResponse ->
            // Filter the tags that contain the serial number
            val filteredList = tagResponse.data.tags.filter { tag ->
                tag.serialNumber.contains(tagSerial)
            }

            // Create a new instance of Data with the filtered list of tags
            val newData = tagResponse.data.copy(
                tags = filteredList
            )

            // Create a new instance of TagsResponse with the new data
            val newTagsResponse = tagResponse.copy(
                data = newData
            )

            return newTagsResponse
        }

        return null
    }

    suspend fun postLostTag(
        body: PostLostTag,
    ) {
        database.loginDao().fetchAllowedUsers().accessToken?.let { token ->
            Repository.postLostTag(token, body, _errorBody, _lostTag)
            lastBlockedTagSerial = body.serialNumber
        }
    }

    suspend fun postFoundTag(
        body: PostLostTag
    ) {
        database.loginDao().fetchAllowedUsers().accessToken?.let { token ->
            Repository.postFoundLostTag(token, body.serialNumber, _errorBody, _lostTag)
        }
    }

}