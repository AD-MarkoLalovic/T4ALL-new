package com.mobility.enp.data.model.api_my_profile.refund_request.tags.response

import com.mobility.enp.data.model.api_my_profile.refund_request.tags.entity.TagsRefundRequestEntity

data class Tag(
    val id: String,
    val category: Category,
    val country: Country,
    val registrationPlate: String,
    val roming: Boolean,
    val serialNumber: String,
    val showButtonFoundTag: Boolean,
    val showButtonLostTag: Boolean,
    val statuses: List<Statuse>
) {
    fun toEntityTagsRefundRequest(): TagsRefundRequestEntity {

        return TagsRefundRequestEntity(
            id = id,
            serialNumber = serialNumber,
            registrationPlate = registrationPlate
        )
    }
}