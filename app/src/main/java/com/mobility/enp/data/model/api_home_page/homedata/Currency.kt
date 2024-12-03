package com.mobility.enp.data.model.api_home_page.homedata


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Currency(
    @SerializedName("label")
    var label: String?,
    @SerializedName("value")
    var value: String?
)