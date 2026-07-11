package com.mobility.enp.data.model.new_toll_history.tags.local

import androidx.room.Entity

@Entity(
    tableName = "toll_history_tags",
    primaryKeys = ["serialNumber"]
)
data class TollHistoryTagsEntity(
    val serialNumber: String,
    val registrationPlate: String?
)
