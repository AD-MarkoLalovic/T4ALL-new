package com.mobility.enp.data.model.api_my_profile.refund_request.tags.response

import com.mobility.enp.data.model.api_my_profile.refund_request.tags.entity.TagsRefundRequestEntity

data class Data(
    val currentPage: Int,
    val lastPage: Int,
    val perPage: Int,
    val tags: List<Tag>,
    val total: Int
) {
    fun toTagsEntityList(): List<TagsRefundRequestEntity> {
        return tags.map { it.toEntityTagsRefundRequest() }
    }
}