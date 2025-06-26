package com.mobility.enp.util

sealed class SubmitResultFold<out T> {
    data class Success<T>(val data: T) : SubmitResultFold<T>()
    object Loading : SubmitResultFold<Nothing>()
    data class Failure(val error: Throwable) : SubmitResultFold<Nothing>()
    object Idle : SubmitResultFold<Nothing>()

}

