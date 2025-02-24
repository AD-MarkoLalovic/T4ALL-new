package com.mobility.enp.data.model.cardsweb


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class ShowSubscriptionNotification(
    @SerializedName("ME")
    val mE: Boolean?,
    @SerializedName("MK")
    val mK: Boolean?
)