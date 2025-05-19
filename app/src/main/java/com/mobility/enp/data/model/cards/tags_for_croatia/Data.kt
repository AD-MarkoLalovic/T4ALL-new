package com.mobility.enp.data.model.cards.tags_for_croatia

data class Data(
    val currentPage: Int?,
    val lastPage: Int?,
    val perPage: Int?,
    val tags: List<Tag>,
    val total: Int?
)