package com.mobility.enp.data.model.api_my_invoices.refactor


import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
data class AllowedCountry(
    @SerializedName("name")
    @Expose
    val name: String?,
    @SerializedName("value")
    @Expose
    val value: String?
)