package com.mobility.enp.data.model.new_toll_history.local.entity

import androidx.room.Entity

@Entity(
    tableName = "new_sum_tags",
    primaryKeys = ["tagSerialNumber","currency"]
)
data class SumTagEntity(
    val tagSerialNumber: String,
    val currency: String,
    val total: String,
    val position: Int
)
