package com.mobility.enp.data.model.api_home_page.homedata


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PaymentAmount(
    @SerializedName("amount")
    var amount: String?,
    @SerializedName("currency")
    var currency: String?
)