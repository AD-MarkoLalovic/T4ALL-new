package com.mobility.enp.data.model.home.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.mobility.enp.data.model.home.entity.HomeEntity
import com.mobility.enp.data.model.home.entity.InvoiceHomeEntity
import com.mobility.enp.data.model.home.entity.TollHistoryHomeEntity

data class HomeWithDetails(
    @Embedded val home: HomeEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "homeId"
    )
    val tollHistory: List<TollHistoryHomeEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "homeId",
        entity = InvoiceHomeEntity::class
    )
    val invoice: List<InvoiceWithCurrency>
)