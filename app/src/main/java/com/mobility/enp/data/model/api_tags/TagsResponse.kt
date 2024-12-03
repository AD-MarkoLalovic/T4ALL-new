package com.mobility.enp.data.model.api_tags

import androidx.annotation.Keep

@Keep
data class TagsResponse(
    val data: Data, val message: String
)

@Keep
data class Tag(
    val id: String,
    val serialNumber: String,
    val registrationPlate: String,
    val country: Country,
    val statuses: List<Status>,
    val category: Category,
    var isChecked: Boolean,
    val showButtonLostTag: Boolean,
    val showButtonFoundTag: Boolean
)

@Keep
data class Country(
    val value: String,
    val text: String
)

@Keep
data class Status(
    val country: Country,
    val status: StatusDetail
)

@Keep
data class StatusDetail(
    var value: Int,
    var text: String
)

@Keep
data class Category(
    val value: Int,
    val text: String
)

@Keep
data class Data(
    val tags: List<Tag>,
    val total: Int,
    val currentPage: Int,
    val perPage: Int,
    val lastPage: Int
)
