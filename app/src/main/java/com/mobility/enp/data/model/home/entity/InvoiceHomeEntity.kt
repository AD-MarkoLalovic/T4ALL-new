package com.mobility.enp.data.model.home.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mobility.enp.view.ui_models.home.HomeInvoicesUI

@Entity(tableName = "invoices_home")
data class InvoiceHomeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val month: String,
    val year: String,
    val total: String,
    val isPaid: Boolean,
    val currency: String
) {
    fun toHomeInvoicesUI(): HomeInvoicesUI {
        return HomeInvoicesUI(
            month = month,
            total = "$total $currency"
        )
    }

}
