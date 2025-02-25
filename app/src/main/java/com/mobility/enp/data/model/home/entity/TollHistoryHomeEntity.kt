package com.mobility.enp.data.model.home.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "toll_history_home",
    foreignKeys = [ForeignKey(
        entity = HomeEntity::class, // Ova tabela je povezana sa HomeEntity
        parentColumns = ["id"], // Kolona "id" u HomeEntity je primarni ključ
        childColumns = ["homeId"],  // Kolona "homeId" u TollHistoryEntity referencira taj "id"
        onDelete = ForeignKey.CASCADE // Kada se obriše HomeEntity, brišu se i svi povezani zapisi
    )],
    primaryKeys = ["homeId", "invoiceNumber"]
)
data class TollHistoryHomeEntity(
    val homeId: Int,
    val invoiceNumber: String,
    val status: Int?,
    val entryToll: String?,
    val exitToll: String?,
    val entryDate: String?,
    val exitDate: String?,
    val entryTime: String?,
    val exitTime: String?,
    val paymentAmount: String?,
    val paymentCurrency: String?
)