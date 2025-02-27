package com.mobility.enp.data.model.home.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home_entity")
data class HomeEntity(
    @PrimaryKey val id: Int = 1,
    val firstName: String?,
    val lastName: String?,
    val displayName: String,
    val customerType: Int,
    val portalKey: String?
)
