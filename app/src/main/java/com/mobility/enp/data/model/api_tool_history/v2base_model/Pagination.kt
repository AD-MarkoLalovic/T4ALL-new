package com.mobility.enp.data.model.api_tool_history.v2base_model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.google.gson.annotations.Expose

@Keep
data class Pagination(
    @SerializedName("current_page")
    @Expose
    val currentPage: Int,
    @SerializedName("first_page")
    @Expose
    val firstPage: Int,
    @SerializedName("last_page")
    @Expose
    val lastPage: Int,
    @SerializedName("next_page")
    @Expose
    val nextPage: Int,
    @SerializedName("per_page")
    @Expose
    val perPage: Int,
    @SerializedName("prev_page")
    @Expose
    val prevPage: Int,
    @SerializedName("total")
    @Expose
    val total: Int
)