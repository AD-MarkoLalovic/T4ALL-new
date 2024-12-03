package com.mobility.enp.data.model.api_home_page.homedata


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class TollHistory(
    @SerializedName("entryDate")
    var entryDate: String?,
    @SerializedName("entryTime")
    var entryTime: String?,
    @SerializedName("entryToll")
    var entryToll: String?,
    @SerializedName("exitDate")
    var exitDate: String?,
    @SerializedName("exitTime")
    var exitTime: String?,
    @SerializedName("exitToll")
    var exitToll: String?,
    @SerializedName("invoiceNumber")
    var invoiceNumber: String?,
    @SerializedName("paymentAmount")
    var paymentAmount: PaymentAmount?,
    @SerializedName("status")
    var status: Status
)