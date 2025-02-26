package com.mobility.enp.data.model.cardsweb


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class CountryXXX(
    @SerializedName("me")
    val me: String?,
    @SerializedName("mk")
    val mk: String?,
    @SerializedName("rs")
    val rs: String?
)