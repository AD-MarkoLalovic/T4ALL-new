package com.mobility.enp.data.model.api_tool_history.index


import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
data class Country(
    @SerializedName("text")
    @Expose
    val text: String? = null,
    @SerializedName("value")
    @Expose
    val value: String? = null
)