package com.mobility.enp.data.model.home.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mobility.enp.view.ui_models.home.HomeTollHistoryUI

@Entity(tableName = "toll_history_home")
data class TollHistoryHomeEntity(
    @PrimaryKey(autoGenerate = false)
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
) {
    fun toHomeTollHistoryUI(): HomeTollHistoryUI {
        return HomeTollHistoryUI(
            invoiceNumber = invoiceNumber,
            entryToll = entryToll,
            exitToll = exitToll,
            entryDataAndTime = "$entryDate  $entryTime",
            exitDateAndTime = "$exitDate  $exitTime",
            payment = "$paymentAmount $paymentCurrency"
        )
    }
}