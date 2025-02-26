package com.mobility.enp.data.model.cardsweb


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class Country(
    @SerializedName("code")
    val code: String?,
    @SerializedName("name")
    val name: String?
)