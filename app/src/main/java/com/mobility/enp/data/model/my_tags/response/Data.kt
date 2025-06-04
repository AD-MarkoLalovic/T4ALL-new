package com.mobility.enp.data.model.my_tags.response

data class Data(
    val currentPage: Int?,
    val lastPage: Int?,
    val perPage: Int?,
    val tags: List<MyTagsList>?,
    val total: Int?
)