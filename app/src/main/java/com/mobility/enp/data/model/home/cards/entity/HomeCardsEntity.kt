package com.mobility.enp.data.model.home.cards.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home_cards")
data class HomeCardsEntity(
    @PrimaryKey
    val id: String,
    val code: String,
    val title: String,
    var description: String,
    var deletedByUser: Boolean = false,
    var time: Long = System.currentTimeMillis()
)
