package com.mobility.enp.viewmodel.toll_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobility.enp.MyApplication
import com.mobility.enp.data.model.new_toll_history.complaint.ComplaintBodyNew
import com.mobility.enp.data.repository.ComplaintAndObjectionRepository
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.view.ui_models.BankUIModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ComplaintViewModel(private val repo: ComplaintAndObjectionRepository) : ViewModel() {

    private val _submitComplaint = MutableStateFlow<SubmitResult<Unit>>(SubmitResult.Empty)
    val submitComplaint: StateFlow<SubmitResult<Unit>> = _submitComplaint.asStateFlow()

    val banks: StateFlow<List<BankUIModel>> = repo.observeBanks()
        .map { list ->
            list.map { it.toBanksUIModel() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    init {
        viewModelScope.launch {
            repo.refreshBank()
        }
    }

    fun validate(
        licencePlate: String,
        reasonForComplaint: String,
        showBankForm: Boolean,
        selectedBankPosition: Int,
        uniqueNumber: String,
        centerAccountNumber: String,
        rightAccountNumber: String
    ): ComplaintValidationResult {

        if (licencePlate.isEmpty() || reasonForComplaint.isEmpty()) {
            return ComplaintValidationResult.EmptyRequiredFields
        }

        if (reasonForComplaint.length <= 10) {
            return ComplaintValidationResult.ReasonTooShort
        }

        if (showBankForm) {
            if (selectedBankPosition == 0) {
                return ComplaintValidationResult.NoBankSelected
            }

            if (uniqueNumber.isEmpty() || centerAccountNumber.isEmpty() || rightAccountNumber.isEmpty()) {
                return ComplaintValidationResult.MissingBankFields
            }

            if (centerAccountNumber.length != 13 || rightAccountNumber.length != 2) {
                return ComplaintValidationResult.InvalidAccountNumber
            }
        }

        return ComplaintValidationResult.Valid
    }

    fun submitComplaint(body: ComplaintBodyNew) {
        viewModelScope.launch {
            _submitComplaint.value = SubmitResult.Loading
            val result = repo.postComplaint(body)
            _submitComplaint.value = result.fold(
                onSuccess = {
                    SubmitResult.Success(Unit)
                },
                onFailure = { error ->
                    when (error) {
                        is NetworkError.NoConnection -> SubmitResult.FailureNoConnection
                        is NetworkError.ApiError -> when (error.errorResponse.code) {
                            401, 405 -> SubmitResult.InvalidApiToken(
                                error.errorResponse.code,
                                error.errorResponse.message ?: ""
                            )

                            else -> SubmitResult.FailureApiError(error.errorResponse.message ?: "")
                        }

                        else -> SubmitResult.FailureServerError
                    }
                }
            )
        }
    }

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                ComplaintViewModel(app.complaintAndObjectionRepository)
            }
        }
    }
}