package com.mobility.enp.data.model.api_home_page.homedata


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Status(
    @SerializedName("text")
    var text: String?,
    @SerializedName("value")
    var value: Int?
)