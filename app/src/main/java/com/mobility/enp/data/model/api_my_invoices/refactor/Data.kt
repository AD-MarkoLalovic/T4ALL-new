package com.mobility.enp.data.model.api_my_invoices.refactor


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.google.gson.annotations.Expose

@Keep
data class Data(
    @SerializedName("allowedCountries")
    @Expose
    val allowedCountries: List<AllowedCountry>?,
    @SerializedName("currentPage")
    @Expose
    val currentPage: Int,
    @SerializedName("lastPage")
    @Expose
    val lastPage: Int,
    @SerializedName("months")
    @Expose
    val months: List<Month>,
    @SerializedName("perPage")
    @Expose
    val perPage: Int,
    @SerializedName("total")
    @Expose
    val total: Int
)