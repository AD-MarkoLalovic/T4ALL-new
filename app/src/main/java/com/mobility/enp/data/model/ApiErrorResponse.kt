package com.mobility.enp.data.model

data class ApiErrorResponse(
    val message: String?,
    val errors: Any?,
    val code: Int? = null
)
