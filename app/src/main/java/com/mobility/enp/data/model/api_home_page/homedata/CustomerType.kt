package com.mobility.enp.data.model.api_home_page.homedata


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CustomerType(
    @SerializedName("name")
    var name: String?,
    @SerializedName("type")
    var type: Int?
)