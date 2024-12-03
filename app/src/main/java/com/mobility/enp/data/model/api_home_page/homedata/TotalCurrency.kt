package com.mobility.enp.data.model.api_home_page.homedata


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


@Keep
data class TotalCurrency(
    @SerializedName("currency")
    var currency: Currency?,
    @SerializedName("isPaid")
    var isPaid: Boolean?,
    @SerializedName("total")
    var total: String?
)