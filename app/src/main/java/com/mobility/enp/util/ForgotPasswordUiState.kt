package com.mobility.enp.util

sealed class ForgotPasswordUiState {
    object Success : ForgotPasswordUiState()
    object Idle : ForgotPasswordUiState()
    data class Failure(val error: Throwable) : ForgotPasswordUiState()
    object Loading : ForgotPasswordUiState()
}
