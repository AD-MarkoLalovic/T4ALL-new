package com.mobility.enp.data.model.api_tool_history.v2base_model


import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
data class Bill(
    @SerializedName("bill_final")
    @Expose
    val billFinal: String?,
    @SerializedName("country_code")
    @Expose
    val countryCode: String?,
    @SerializedName("currency")
    @Expose
    val currency: String?,
    @SerializedName("discount")
    @Expose
    val discount: String?,
    @SerializedName("id")
    @Expose
    val id: String?,
    @SerializedName("paid")
    @Expose
    val paid: String
)