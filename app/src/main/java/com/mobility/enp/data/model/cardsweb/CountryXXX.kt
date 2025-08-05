package com.mobility.enp.data.model.cardsweb


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CountryXXX(
    @SerializedName("me")
    val me: String?,
    @SerializedName("mk")
    val mk: String?,
    @SerializedName("rs")
    val rs: String?
)