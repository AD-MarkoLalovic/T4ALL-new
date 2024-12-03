package com.mobility.enp.data.model.api_my_profile

import androidx.annotation.Keep

@Keep
data class ChangePasswordRequest(
    val oldPassword: String?,
    val newPassword: String?,
    val newPasswordConfirmation: String?
)
