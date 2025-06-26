package com.mobility.enp.data.model.api_my_profile.my_tags.response

data class Pagination(
    val current_page: Int?,
    val first_page: Int?,
    val last_page: Int?,
    val next_page: Int?,
    val per_page: Int?,
    val prev_page: Int?,
    val total: Int?
)