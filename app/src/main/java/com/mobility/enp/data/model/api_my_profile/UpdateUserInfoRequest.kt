package com.mobility.enp.data.model.api_my_profile

import androidx.annotation.Keep

@Keep
data class UpdateUserInfoRequest(
    val firstName: String?,
    val lastName: String?,
    val phone: String?,
    val address: String?,
    val city: String?,
    val postalCode: String?,
    val companyName: String?,
    val mb: String?
)


