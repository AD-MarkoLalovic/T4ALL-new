package com.mobility.enp.data.model.api_home_page.homedata


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Customer(
    @SerializedName("customerType")
    var customerType: CustomerType?,
    @SerializedName("displayName")
    var displayName: String?,
    @SerializedName("firstName")
    var firstName: String?,
    @SerializedName("lastName")
    var lastName: String?
)