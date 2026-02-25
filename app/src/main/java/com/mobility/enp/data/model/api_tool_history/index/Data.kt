package com.mobility.enp.data.model.api_tool_history.index


import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
data class Data(
    @SerializedName("currentPage")
    @Expose
    var currentPage: Int? = 0,
    @SerializedName("lastPage")
    @Expose
    var lastPage: Int? = 0,
    @SerializedName("perPage")
    @Expose
    var perPage: Int? = 0,
    @SerializedName("tags")
    @Expose
    var tags: List<Tag>? = listOf(),
    @SerializedName("total")
    @Expose
    var total: Int? = 0
)