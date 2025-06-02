package com.mobility.enp.data.model.api_tool_history.v2base_model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.google.gson.annotations.Expose

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