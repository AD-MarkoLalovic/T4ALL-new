package com.mobility.enp.view.ui_models

data class BasicInfoUIModel(
    val address: String,
    val city: String,
    val companyName: String?,
    val countryName: String,
    val customerType: Int,
    val displayName: String?,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val mb: String?,
    val phone: String,
    val postalCode: String?
)
