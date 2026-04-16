package com.mobility.enp.data.model.new_toll_history.local.entity

import androidx.room.Entity

@Entity(
    tableName = "new_toll_history_items",
    primaryKeys = ["rowId"]
)
data class TollHistoryItemEntity(
    val rowId: String,
    val rowType: Int,
    val sortIndex: Int,
    val id: Int,
    val tagsSerialNumber: String,
    val tollPlaza: String,
    val isPaid: Boolean,
    val checkInDate: Long?,
    val checkOutDate: Long?,
    val amountWithOutDiscount: String,
    val currency: String,
    val billFinal: String,
    val objectionCount: Int,
    val complaintId: Int?,
    val filterCountry: String,
    val tagTotal: String,
    val tagCurrency: String
    ) {

    companion object{
        const val ROW_TYPE_HEADER    = 0
        const val ROW_TYPE_PASSAGE   = 1
        const val ROW_TYPE_GROUP_END = 2
    }
}
