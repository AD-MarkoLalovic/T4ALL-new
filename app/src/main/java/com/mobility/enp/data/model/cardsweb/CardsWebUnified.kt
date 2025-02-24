package com.mobility.enp.data.model.cardsweb


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class CardsWebUnified(
    @SerializedName("active")
    val active: Int?,
    @SerializedName("cardType")
    val cardType: String?,
    @SerializedName("country")
    val country: Country?,
    @SerializedName("default_card")
    val defaultCard: Int?,
    @SerializedName("details")
    val details: String?,
    @SerializedName("expiration_date")
    val expirationDate: String?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("image_url")
    val imageUrl: String?
)