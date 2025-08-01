package com.mobility.enp.data.model.api_tool_history.index


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.google.gson.annotations.Expose

@Keep
data class Data(
    @SerializedName("currentPage")
    @Expose
    val currentPage: Int? = 0,
    @SerializedName("lastPage")
    @Expose
    val lastPage: Int? = 0,
    @SerializedName("perPage")
    @Expose
    val perPage: Int? = 0,
    @SerializedName("tags")
    @Expose
    var tags: List<Tag>? = listOf(),
    @SerializedName("total")
    @Expose
    val total: Int? = 0
)