package com.mobility.enp.util

import com.mobility.enp.data.model.ApiErrorResponse

sealed class NetworkError : Throwable() {
    object NoConnection : NetworkError()
    object ServerError : NetworkError()
    data class ApiError(val errorResponse: ApiErrorResponse) : NetworkError()
}