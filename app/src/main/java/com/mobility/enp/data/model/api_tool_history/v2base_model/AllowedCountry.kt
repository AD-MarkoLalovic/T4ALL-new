package com.mobility.enp.data.model.api_tool_history.v2base_model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.google.gson.annotations.Expose

@Keep
data class AllowedCountry(
    @SerializedName("name")
    @Expose
    val name: String?,
    @SerializedName("value")
    @Expose
    val value: String?
)