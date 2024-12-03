package com.mobility.enp.data.model.api_home_page.homedata


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


@Keep
data class Data(
    @SerializedName("customer")
    var customer: Customer?,
    @SerializedName("invoices")
    var invoices: List<Invoice>,
    @SerializedName("tollHistory")
    var tollHistory: List<TollHistory>
)