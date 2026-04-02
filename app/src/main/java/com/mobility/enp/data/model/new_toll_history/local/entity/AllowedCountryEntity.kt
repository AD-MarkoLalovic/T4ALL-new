package com.mobility.enp.data.model.new_toll_history.local.entity

import androidx.room.Entity

@Entity(
    tableName = "new_allowed_countries",
    primaryKeys = ["value"]
)
data class AllowedCountryEntity(
    val value: String,
    val name: String,
    val position: Int
)