package com.mobility.enp.util

sealed class SubmitResult<out T> {
    data class Success<T>(val data: T) : SubmitResult<T>()
    object Empty : SubmitResult<Nothing>() // Za praznu listu
    object FailureServerError : SubmitResult<Nothing>() // Greška povezana sa API-jem
    object FailureNoConnection : SubmitResult<Nothing>() // Greška bez veze
    object Loading : SubmitResult<Nothing>() // Loading sada podržava sve tipove
    data class FailureApiError(val errorMessage: String) : SubmitResult<Nothing>()
}

