package com.mobility.enp.data.model.api_tool_history.index


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.google.gson.annotations.Expose

@Keep
data class Category(
    @SerializedName("text")
    @Expose
    val text: String? = null,
    @SerializedName("value")
    @Expose
    val value: Int? = null
)