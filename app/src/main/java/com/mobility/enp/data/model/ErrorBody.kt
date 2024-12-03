package com.mobility.enp.data.model

import androidx.annotation.Keep

@Keep
data class ErrorBody(
    val errorCode: Int,
    var errorBody: String
)
