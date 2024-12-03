package com.mobility.enp.data.model.banks.response

import com.google.gson.annotations.SerializedName
import com.mobility.enp.data.model.banks.entity.BanksEntity

data class Results(
    @SerializedName("current_page")
    val currentPage: Int,
    val `data`: List<DataX>,
    @SerializedName("first_page_url")
    val firstPageUrl: String,
    val from: Int,
    @SerializedName("last_page")
    val lastPage: Int,
    @SerializedName("last_page_url")
    val lastPageUrl: String,
    val links: List<Link>,
    @SerializedName("next_page_url")
    val nextPageUrl: Any,
    val path: String,
    @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("prev_page_url")
    val prevPageUrl: Any,
    val to: Int,
    val total: Int
) {
    fun toListBanksEntity(): List<BanksEntity> {
        return data.map { it.toBanksEntity() }

    }
}