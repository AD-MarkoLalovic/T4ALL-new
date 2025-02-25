package com.mobility.enp.data.model.home.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoices_home",
    foreignKeys = [ForeignKey(
        entity = HomeEntity::class,
        parentColumns = ["id"],
        childColumns = ["homeId"],
        onDelete = ForeignKey.CASCADE
    )])
data class InvoiceHomeEntity(
    @PrimaryKey
    val homeId: Int,
    val monthName: String,
    val year: String,
)
