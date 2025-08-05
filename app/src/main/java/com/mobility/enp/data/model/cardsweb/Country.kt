package com.mobility.enp.data.model.cardsweb


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Country(
    @SerializedName("code")
    val code: String?,
    @SerializedName("name")
    val name: String?
)