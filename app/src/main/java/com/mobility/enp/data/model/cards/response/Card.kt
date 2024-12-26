package com.mobility.enp.data.model.cards.response

data class Card(
    val active: Int?,
    val cardDetails: String?,
    val cardType: String?,
    val country: Country?,
    val defaultCard: Int?,
    val id: Int?
)