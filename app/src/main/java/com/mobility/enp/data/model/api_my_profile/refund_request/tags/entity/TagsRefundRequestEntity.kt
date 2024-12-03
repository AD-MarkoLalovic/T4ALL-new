package com.mobility.enp.data.model.api_my_profile.refund_request.tags.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mobility.enp.view.ui_models.refund_request.TagsRefundRequestUIModel

@Entity(tableName = "tags_refund_request")
data class TagsRefundRequestEntity (
    @PrimaryKey
    val id: String,
    val serialNumber: String,
    val registrationPlate: String
) {
    fun toTagsRefundRequestUIModel(): TagsRefundRequestUIModel {
        return TagsRefundRequestUIModel(
            id = id,
            serialNumber = serialNumber,
            registrationPlate = registrationPlate,
            isChecked = false
        )
    }
}