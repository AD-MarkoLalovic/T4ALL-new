package com.mobility.enp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobility.enp.MyApplication
import com.mobility.enp.data.repository.UserRepository
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.view.ui_models.refund_request.RefundRequestUIModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.launch

class RefundRequestViewModel(private val repository: UserRepository) : ViewModel() {

    private val _refundRequestUI = MutableStateFlow<SubmitResult<List<RefundRequestUIModel>>>(SubmitResult.Loading)
    val refundRequestUI: StateFlow<SubmitResult<List<RefundRequestUIModel>>> = _refundRequestUI

    init {
        fetchRefundRequests()
    }

    private fun fetchRefundRequests() {
        viewModelScope.launch {
            // Postavi stanje na Loading pre nego što započneš operaciju
            _refundRequestUI.value = SubmitResult.Loading

            // Prvo dohvati lokalne podatke iz baze
            val localData = repository.getLocalRefundRequests()
            if (localData.isNotEmpty()) {
                _refundRequestUI.value = SubmitResult.Success(localData.map { it.toUIModel() })
            }

            // Dobavi refund zahteve sa servera
            val result = repository.getRefundRequestFromServer()

            if (result.isSuccess) {
                // Ako je uspešno, prikaži podatke
                val refundRequestsEntity = result.getOrNull() ?: emptyList()

                if (refundRequestsEntity.isEmpty()) {
                    // Ako je lista prazna, postavi odgovarajuće stanje
                    _refundRequestUI.value = SubmitResult.Empty // Postavi Empty stanje
                } else {
                    // Mapiraj entitete u UI modele i ažuriraj stanje
                    val uiModels = refundRequestsEntity.map { it.toUIModel() }
                    _refundRequestUI.value = SubmitResult.Success(uiModels)
                }
            } else {
                // Ako je neuspešno, prikaži grešku
                when (val error = result.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.e(
                            "RefundRequestViewModel",
                            "Greška tokom preuzimanja refund zahteva",
                            error
                        )
                        _refundRequestUI.value =
                            SubmitResult.FailureServerError // Postavi FailureApiError
                    }

                    is NetworkError.NoConnection -> {
                        _refundRequestUI.value =
                            SubmitResult.FailureNoConnection // Postavi FailureNoConnection
                    }
                    is NetworkError.ApiError -> {
                            _refundRequestUI.value = SubmitResult.FailureApiError(error.errorResponse.message!!)
                    }
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepository = (this[APPLICATION_KEY] as MyApplication).repositoryUser
                RefundRequestViewModel(
                    repository = myRepository
                )
            }
        }
    }

}