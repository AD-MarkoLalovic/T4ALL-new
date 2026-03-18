package com.mobility.enp.data.model

import com.google.gson.annotations.SerializedName

data class TagOrderInputs(
    @SerializedName("customer_type")
    val customerType: Int,
    val city: String,
    @SerializedName("postal_code")
    val postalCode: String?,
    val email: String,
    val phone: String,
    @SerializedName("first_name")
    val firstName: String?,
    @SerializedName("last_name")
    val lastName: String?,
    @SerializedName("company_name")
    val companyName: String?,
    val mb: String?,
    val pib: String?
)
