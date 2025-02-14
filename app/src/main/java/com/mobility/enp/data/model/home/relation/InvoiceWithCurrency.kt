package com.mobility.enp.data.model.home.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.mobility.enp.data.model.home.entity.InvoiceHomeEntity
import com.mobility.enp.data.model.home.entity.InvoiceHomeTotalCurrencyEntity

data class InvoiceWithCurrency(
    @Embedded val invoice: InvoiceHomeEntity,

    @Relation(
        parentColumn = "homeId",
        entityColumn = "invoiceId"
    )
    val invoiceDetails: List<InvoiceHomeTotalCurrencyEntity>
)
