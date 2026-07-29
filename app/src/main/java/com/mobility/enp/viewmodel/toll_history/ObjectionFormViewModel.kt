package com.mobility.enp.viewmodel.toll_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobility.enp.MyApplication
import com.mobility.enp.data.model.new_toll_history.objection.ObjectionBodyNew
import com.mobility.enp.data.repository.ComplaintAndObjectionRepository
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ObjectionFormViewModel(val repository: ComplaintAndObjectionRepository) : ViewModel() {

    private val _submitObjectionState = MutableStateFlow<SubmitResult<Unit>>(SubmitResult.Empty)
    val submitObjectionState = _submitObjectionState.asStateFlow()

    fun validateObjectionForm(itemData: String, itemReason: String): ObjectionValidationResult {
        if (itemData.isEmpty() || itemReason.isEmpty()) return ObjectionValidationResult.EmptyRequiredFields
        if (itemReason.length <= 10) return ObjectionValidationResult.ReasonTooShort

        return ObjectionValidationResult.Valid
    }

    fun submitObjection(body: ObjectionBodyNew) {
        viewModelScope.launch {
            _submitObjectionState.value = SubmitResult.Loading

            val result = repository.postObjectionNew(body)

            _submitObjectionState.value = result.fold(
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
                ObjectionFormViewModel(repository = app.complaintAndObjectionRepository)
            }
        }
    }
}