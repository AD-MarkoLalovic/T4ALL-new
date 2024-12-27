package com.mobility.enp.data.model.cards.response

data class CardsResponse(
    val `data`: List<Card>?,
    val message: String? = null
)