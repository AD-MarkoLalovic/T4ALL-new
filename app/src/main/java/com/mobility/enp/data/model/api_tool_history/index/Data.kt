package com.mobility.enp.data.model.api_tool_history.index


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Data(
    @SerializedName("tags")
    var tags: List<Tag?>?
)