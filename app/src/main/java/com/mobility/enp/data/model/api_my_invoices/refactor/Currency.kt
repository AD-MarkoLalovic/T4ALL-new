package com.mobility.enp.data.model.api_my_invoices.refactor


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.google.gson.annotations.Expose

@Keep
data class Currency(
    @SerializedName("label")
    @Expose
    val label: String?,
    @SerializedName("value")
    @Expose
    val value: String?
)