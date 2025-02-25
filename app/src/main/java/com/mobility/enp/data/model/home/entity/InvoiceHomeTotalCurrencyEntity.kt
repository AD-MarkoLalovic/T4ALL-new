package com.mobility.enp.data.model.home.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "invoices_home_total_currency",
    foreignKeys = [ForeignKey(
        entity = InvoiceHomeEntity::class,
        parentColumns = ["homeId"],
        childColumns = ["invoiceId"],
        onDelete = ForeignKey.CASCADE
    )],
    primaryKeys = ["total", "currencyValue"]
)
data class InvoiceHomeTotalCurrencyEntity(
    val invoiceId: Int,
    val total: String,
    val isPaid: Boolean,
    val currencyValue: String
) {
    val totalAndCurrency: String
        get() = "$total $currencyValue"
}
