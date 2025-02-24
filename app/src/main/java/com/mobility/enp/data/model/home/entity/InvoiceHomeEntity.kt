package com.mobility.enp.data.model.home.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "invoices_home",
    foreignKeys = [ForeignKey(
        entity = HomeEntity::class,
        parentColumns = ["id"],
        childColumns = ["homeId"],
        onDelete = ForeignKey.CASCADE
    )],
    primaryKeys = ["monthName", "year"], // Kombinovani primarni ključ
    indices = [Index(value = ["homeId"], unique = true)] // Dodavanje jedinstvenog indeksa na homeId
    //jer homeId mora biti jedisntven zbog ForeignKey
)
data class InvoiceHomeEntity(
    val homeId: Int,
    val monthName: String,
    val year: String,
)
