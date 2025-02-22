package com.mobility.enp.data.model.home.cards.added_cards.response

import com.mobility.enp.data.model.home.cards.added_cards.entity.AddedCardsEntity
import com.mobility.enp.util.toEntityAddedCards

data class AddedCardsResponse(
    val `data`: List<CardsList?>,
    val message: String?
) {
    fun toEntity(): List<AddedCardsEntity> {
        return data.mapNotNull { it?.toEntityAddedCards() }
    }
}