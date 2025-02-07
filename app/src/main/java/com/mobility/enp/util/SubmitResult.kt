package com.mobility.enp.util

sealed class SubmitResult<out T> {
    data class Success<T>(val data: T) : SubmitResult<T>()
    object Empty : SubmitResult<Nothing>()
    object FailureServerError : SubmitResult<Nothing>()
    object FailureNoConnection : SubmitResult<Nothing>()
    object Loading : SubmitResult<Nothing>()
    data class FailureApiError(val errorMessage: String) : SubmitResult<Nothing>()
    data class InvalidApiToken(val errorCode: Int, val errorMessage: String) : SubmitResult<Nothing>()

}

