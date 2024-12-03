package com.mobility.enp.data.model.login

import androidx.annotation.Keep

@Keep
data class LoginBody(
    val email: String?,
    val password: String?
)
