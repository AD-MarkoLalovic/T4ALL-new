package com.mobility.enp.data.model.api_tool_history.v2base_model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.google.gson.annotations.Expose

@Keep
data class Customer(
    @SerializedName("address")
    @Expose
    val address: String?,
    @SerializedName("country")
    @Expose
    val country: String?,
    @SerializedName("phone")
    @Expose
    val phone: String?
)