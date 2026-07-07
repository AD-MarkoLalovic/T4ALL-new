package com.mobility.enp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobility.enp.MyApplication
import com.mobility.enp.data.model.api_my_profile.refund_request.SendRefundRequest
import com.mobility.enp.data.repository.UserRepository
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.view.ui_models.BankUIModel
import com.mobility.enp.view.ui_models.refund_request.TagsRefundRequestUIModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TagPickerRequestFormState(
    val amount: String = "",
    val bankPosition: Int = 0,
    val uniqueNumberPosition: Int = -1,
    val centerAccountNumber: String = "",
    val rightAccountNumber: String = "",
    val tagSerialNumber: String? = null
)

class TagPickerRequestViewModel(private val repository: UserRepository) : ViewModel() {

    var formState: TagPickerRequestFormState = TagPickerRequestFormState()
        private set

    fun updateFormState(state: TagPickerRequestFormState) {
        formState = state
    }

    fun clearAmount() {
        formState = formState.copy(amount = "")
    }

    fun clearTagSerialNumber() {
        formState = formState.copy(tagSerialNumber = null)
    }

    fun clearBankPosition() {
        formState = formState.copy(bankPosition = 0)
    }

    fun clearUniqueNumberPosition() {
        formState = formState.copy(uniqueNumberPosition = -1)
    }

    fun clearCenterAccountNumber() {
        formState = formState.copy(centerAccountNumber = "")
    }

    fun clearRightAccountNumber() {
        formState = formState.copy(rightAccountNumber = "")
    }

    private val _tagPickerRequest = MutableStateFlow<SubmitResult<List<TagsRefundRequestUIModel>>>(
        SubmitResult.Empty
    )
    val tagPickerRequest: StateFlow<SubmitResult<List<TagsRefundRequestUIModel>>> =
        _tagPickerRequest

    private val _banks = MutableStateFlow<SubmitResult<List<BankUIModel>>>(SubmitResult.Empty)
    val banks: StateFlow<SubmitResult<List<BankUIModel>>> = _banks

    private val _refundRequestState = MutableStateFlow<SubmitResult<Unit>>(SubmitResult.Empty)
    val refundRequestState: StateFlow<SubmitResult<Unit>> = _refundRequestState

    init {
        fetchTagsRefundRequest()
        fetchBanks()
    }

    suspend fun existLocalData(): Boolean {
        val data = repository.getLocalTagsRefundRequest()
        return data.isNotEmpty()
    }

    private fun fetchTagsRefundRequest() {
        viewModelScope.launch {

            _tagPickerRequest.value = SubmitResult.Loading

            val localData = repository.getLocalTagsRefundRequest()
            if (localData.isNotEmpty()) {
                _tagPickerRequest.value =
                    SubmitResult.Success(localData.map { it.toTagsRefundRequestUIModel() })
            }

            val remoteData = repository.getTagsRefundRequest()
            if (remoteData.isSuccess) {
                val tagsEntity = remoteData.getOrNull() ?: emptyList()

                if (tagsEntity.isEmpty()) {
                    _tagPickerRequest.value = SubmitResult.Empty
                } else {
                    val uiModel = tagsEntity.map { it.toTagsRefundRequestUIModel() }
                    _tagPickerRequest.value = SubmitResult.Success(uiModel)
                }
            } else {
                when (val error = remoteData.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.e(
                            "TagPickerRequestViewModel",
                            "Greška tokom preuzimanja refund zahteva",
                            error
                        )
                        _tagPickerRequest.value =
                            SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _tagPickerRequest.value =
                            SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        _tagPickerRequest.value =
                            SubmitResult.FailureApiError(error.errorResponse.message!!)
                    }
                }
            }
        }
    }

    private fun fetchBanks() {
        viewModelScope.launch {

            _banks.value = SubmitResult.Loading

            val localBanks = repository.getLocalBanks()
            if (localBanks.isNotEmpty()) {
                _banks.value = SubmitResult.Success(localBanks.map { it.toBanksUIModel() })
            }

            val remoteBanks = repository.getBanksFromServer()
            if (remoteBanks.isSuccess) {
                val banksEntity = remoteBanks.getOrNull() ?: emptyList()
                if (banksEntity.isEmpty()) {
                    _banks.value = SubmitResult.Empty
                } else {
                    val uiModel = banksEntity.map { it.toBanksUIModel() }
                    _banks.value = SubmitResult.Success(uiModel)
                }
            } else {
                when (val error = remoteBanks.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.e(
                            "TagPickerRequestViewModel",
                            "Greška tokom preuzimanja liste banaka",
                            error
                        )
                        _tagPickerRequest.value =
                            SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _tagPickerRequest.value =
                            SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        _tagPickerRequest.value =
                            SubmitResult.FailureApiError(error.errorResponse.message!!)
                    }
                }
            }
        }
    }

    fun postRefundsRequest(refundRequest: SendRefundRequest) {
        viewModelScope.launch {
            _refundRequestState.value = SubmitResult.Loading

            val result = repository.submitRefundRequest(refundRequest)
            if (result.isSuccess) {
                _refundRequestState.value = SubmitResult.Success(Unit)
            } else {
                when (val error = result.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.e(
                            "TagPickerRequestViewModel",
                            "Greška tokom postRefundsRequest ",
                            error
                        )
                        _refundRequestState.value =
                            SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _refundRequestState.value =
                            SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        _refundRequestState.value =
                            SubmitResult.FailureApiError(error.errorResponse.message.toString())
                        Log.d(
                            "TagPickerRequestViewModel",
                            "postRefundsRequest: ${error.errorResponse.message}"
                        )
                    }
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepository = (this[APPLICATION_KEY] as MyApplication).repositoryUser
                TagPickerRequestViewModel(
                    repository = myRepository
                )
            }
        }
    }

}
