package com.mobility.enp.data.model.home.cards.entity

import androidx.room.Entity

@Entity(
    tableName = "home_cards",
    primaryKeys = ["email", "code"]
)
data class HomeCardsEntity(
    val email: String,
    val code: String,
    val title: String,
    var description: String,
    val additionEnabled: Boolean?,
    var deletedByUser: Boolean = false,
    var time: Long = System.currentTimeMillis()
)
