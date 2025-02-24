package com.mobility.enp.data.model.cardsweb


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class CardWebModel(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?
)