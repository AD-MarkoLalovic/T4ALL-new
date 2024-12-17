package com.mobility.enp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobility.enp.MyApplication
import com.mobility.enp.data.model.api_tags.LostTagResponse
import com.mobility.enp.data.model.api_tool_history.complaint.ComplaintBody
import com.mobility.enp.data.model.api_tool_history.complaint.ObjectionBody
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import com.mobility.enp.data.repository.PassageHistoryRepository
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserPassViewModel(private val repository: PassageHistoryRepository) : ViewModel() {

    private val _baseTagDataState = MutableStateFlow<SubmitResult<IndexData>>(SubmitResult.Loading)
    val baseTagDataState: StateFlow<SubmitResult<IndexData>> get() = _baseTagDataState

    private val _complaintObjectionState =
        MutableStateFlow<SubmitResult<LostTagResponse>>(SubmitResult.Loading)
    val complaintObjectionState: StateFlow<SubmitResult<LostTagResponse>> get() = _complaintObjectionState

    fun setStateIndex(indexData: IndexData) { // from room
        _baseTagDataState.value = SubmitResult.Success(indexData)
    }

    fun getIndexData() {
        _baseTagDataState.value = SubmitResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getIndexData()
            val body = result.getOrNull()
            body?.let { data ->
                if (result.isSuccess) {
                    _baseTagDataState.value = SubmitResult.Success(data)
                } else {
                    when (val error = result.exceptionOrNull()) {
                        is NetworkError.ServerError -> {
                            Log.d(TAG, "Error while fetching tag serial data")
                            _baseTagDataState.value = SubmitResult.FailureServerError
                        }

                        is NetworkError.NoConnection -> {
                            _baseTagDataState.value = SubmitResult.FailureNoConnection
                        }

                        is NetworkError.ApiError -> {
                            _baseTagDataState.value =
                                SubmitResult.FailureApiError(error.errorResponse.message ?: "")
                            Log.d(TAG, "api error ${error.errorResponse.message}")
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    fun postComplaint(complaintBody: ComplaintBody) {
        _complaintObjectionState.value = SubmitResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.postComplaint(complaintBody)
            val body = result.getOrNull()
            body?.let { data ->
                if (result.isSuccess) {
                    _complaintObjectionState.value = SubmitResult.Success(data)
                } else {
                    when (val error = result.exceptionOrNull()) {
                        is NetworkError.ServerError -> {
                            Log.d(TAG, "Error while fetching tag serial data")
                            _baseTagDataState.value = SubmitResult.FailureServerError
                        }

                        is NetworkError.NoConnection -> {
                            _baseTagDataState.value = SubmitResult.FailureNoConnection
                        }

                        is NetworkError.ApiError -> {
                            _baseTagDataState.value =
                                SubmitResult.FailureApiError(error.errorResponse.message ?: "")
                            Log.d(TAG, "api error ${error.errorResponse.message}")
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    fun postObjection(objectionBody: ObjectionBody) {
        _complaintObjectionState.value = SubmitResult.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.postObjection(objectionBody)
            val body = result.getOrNull()
            body?.let { data ->
                if (result.isSuccess) {
                    _complaintObjectionState.value = SubmitResult.Success(data)
                } else {
                    when (val error = result.exceptionOrNull()) {
                        is NetworkError.ServerError -> {
                            Log.d(TAG, "Error while fetching tag serial data")
                            _baseTagDataState.value = SubmitResult.FailureServerError
                        }

                        is NetworkError.NoConnection -> {
                            _baseTagDataState.value = SubmitResult.FailureNoConnection
                        }

                        is NetworkError.ApiError -> {
                            _baseTagDataState.value =
                                SubmitResult.FailureApiError(error.errorResponse.message ?: "")
                            Log.d(TAG, "api error ${error.errorResponse.message}")
                        }

                        else -> {}
                    }
                }
            }
        }
    }


    suspend fun fetchIndexData(): IndexData {
        return withContext(Dispatchers.IO) {
            repository.getIndexDataRoom()
        }
    }

    companion object {
        const val TAG = "PassViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepository = (this[APPLICATION_KEY] as MyApplication).passageHistoryRepository
                UserPassViewModel(
                    repository = myRepository
                )
            }
        }
    }

}