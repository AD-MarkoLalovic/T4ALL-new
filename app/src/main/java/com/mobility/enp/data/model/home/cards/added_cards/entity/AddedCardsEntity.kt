package com.mobility.enp.data.model.home.cards.added_cards.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "added_cards")
data class AddedCardsEntity(
    @PrimaryKey
    val id: Int,
    val countryCode: String
)
