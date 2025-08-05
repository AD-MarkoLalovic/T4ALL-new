package com.mobility.enp.data.model.cardsweb


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CardWebModel(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?
)