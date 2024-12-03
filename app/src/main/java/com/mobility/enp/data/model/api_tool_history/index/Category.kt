package com.mobility.enp.data.model.api_tool_history.index


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Category(
    @SerializedName("text")
    var text: String?,
    @SerializedName("value")
    var value: Int?
)