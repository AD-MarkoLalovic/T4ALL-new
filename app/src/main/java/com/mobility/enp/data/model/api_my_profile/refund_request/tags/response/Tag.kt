package com.mobility.enp.data.model.api_my_profile.refund_request.tags.response

import com.google.gson.annotations.SerializedName
import com.mobility.enp.data.model.api_my_profile.refund_request.tags.entity.TagsRefundRequestEntity

data class Tag(
    val id: String,
    val category: Category,
    val country: Country,
    val registrationPlate: String,
    @SerializedName("roming")
    val roaming: Boolean,
    val serialNumber: String,
    val showButtonFoundTag: Boolean,
    val showButtonLostTag: Boolean,
    val statuses: List<Statuse>
) {
    fun toEntityTagsRefundRequest(): TagsRefundRequestEntity? {

        if (roaming) return null

        return TagsRefundRequestEntity(
            id = id,
            serialNumber = serialNumber,
            registrationPlate = registrationPlate
        )
    }
}