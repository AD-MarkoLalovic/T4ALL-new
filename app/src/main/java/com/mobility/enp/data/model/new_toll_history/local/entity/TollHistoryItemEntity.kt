package com.mobility.enp.data.model.new_toll_history.local.entity

import androidx.room.Entity

@Entity(
    tableName = "new_toll_history_items",
    primaryKeys = ["id"]
)
data class TollHistoryItemEntity(
    val id: Int,
    val tagsSerialNumber: String,
    val tollPlaza: String,
    val isPaid: Boolean,
    val checkInDate: Long?,
    val checkOutDate: Long?,
    val amountWithOutDiscount: String,
    val currency: String,
    val billFinal: String,         // "737995/2026-web" (iz bill objekta)
    val objectionCount: Int,
    val complaintId: Int?,          // complaint?.id
    val filterCountry: String         // country filter koji sam koristio pri fetchu
    )
