package com.mobility.enp.data.model.api_my_invoices.refactor


import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

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