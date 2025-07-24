package com.mobility.enp.data.model.api_my_invoices.refactor


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.google.gson.annotations.Expose

@Keep
data class TotalCurrency(
    @SerializedName("currency")
    @Expose
    val currency: Currency?,
    @SerializedName("isPaid")
    @Expose
    val isPaid: Boolean?,
    @SerializedName("total")
    @Expose
    val total: String?
)