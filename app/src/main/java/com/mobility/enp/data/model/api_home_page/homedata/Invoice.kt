package com.mobility.enp.data.model.api_home_page.homedata


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Invoice(
    @SerializedName("month")
    var month: Month?,
    @SerializedName("totalCurrency")
    var totalCurrency: List<TotalCurrency>,
    @SerializedName("year")
    var year: String?
)