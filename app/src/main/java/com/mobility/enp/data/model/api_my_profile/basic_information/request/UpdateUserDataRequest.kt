package com.mobility.enp.data.model.api_my_profile.basic_information.request

data class UpdateUserDataRequest(
    val address: String,
    val city: String,
    val companyName: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val mb: String? = null,
    val phone: String,
    val postalCode: String = "",
    val pib: String = ""
)
