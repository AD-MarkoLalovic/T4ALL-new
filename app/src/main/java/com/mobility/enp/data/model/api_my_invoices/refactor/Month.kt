package com.mobility.enp.data.model.api_my_invoices.refactor


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.google.gson.annotations.Expose

@Keep
data class Month(
    @SerializedName("month")
    @Expose
    val month: MonthX?,
    @SerializedName("totalCurrency")
    @Expose
    val totalCurrency: List<TotalCurrency>?,
    @SerializedName("year")
    @Expose
    val year: String?
)