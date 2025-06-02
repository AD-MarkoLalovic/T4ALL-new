package com.mobility.enp.data.model.api_tool_history.v2base_model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.google.gson.annotations.Expose

@Keep
data class Records(
    @SerializedName("items")
    @Expose
    val items: List<Item>,
    @SerializedName("pagination")
    @Expose
    val pagination: Pagination?
)