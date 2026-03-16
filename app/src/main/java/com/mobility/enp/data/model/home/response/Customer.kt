package com.mobility.enp.data.model.home.response

data class Customer(
    val firstName: String?,
    val lastName: String?,
    val displayName: String,
    val countryCode: String?,
    val customerType: CustomerType,
    val portalKey: String?
)