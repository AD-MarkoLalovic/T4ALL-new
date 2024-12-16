package com.mobility.enp.util

sealed class SubmitResult<out T> {
    data class Success<T>(val data: T) : SubmitResult<T>()
    data object Empty : SubmitResult<Nothing>() // Za praznu listu
    data object FailureServerError : SubmitResult<Nothing>() // Greška povezana sa API-jem
    data object FailureNoConnection : SubmitResult<Nothing>() // Greška bez veze
    data object Loading : SubmitResult<Nothing>() // Loading sada podržava sve tipove
    data class FailureApiError(val errorMessage: String) : SubmitResult<Nothing>()
}

