package com.mobility.enp.data.model.login

import androidx.annotation.Keep

@Keep
data class ForgotPasswordRequest(
    val email: String
)
