package com.mobility.enp.data.model.api_tool_history.v2base_model


import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
data class SumTag(
    @SerializedName("currency")
    @Expose
    val currency: String?,
    @SerializedName("tagSerialNumber")
    @Expose
    val tagSerialNumber: String?,
    @SerializedName("total")
    @Expose
    val total: String?
)