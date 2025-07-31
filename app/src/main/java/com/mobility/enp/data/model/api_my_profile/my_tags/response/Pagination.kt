package com.mobility.enp.data.model.api_my_profile.my_tags.response

import com.google.gson.annotations.SerializedName

data class Pagination(
    @SerializedName("current_page")
    val currentPage: Int?,

    @SerializedName("first_page")
    val firstPage: Int?,

    @SerializedName("last_page")
    val lastPage: Int?,

    @SerializedName("next_page")
    val nextPage: Int?,

    @SerializedName("per_page")
    val perPage: Int?,

    @SerializedName("prev_page")
    val prevPage: Int?,

    val total: Int?
)
