package com.mobility.enp.data.model.cardsweb


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ShowSubscriptionNotification(
    @SerializedName("ME")
    val mE: Boolean?,
    @SerializedName("MK")
    val mK: Boolean?
)