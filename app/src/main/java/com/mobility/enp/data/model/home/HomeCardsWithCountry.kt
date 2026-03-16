package com.mobility.enp.data.model.home

import com.mobility.enp.data.model.home.cards.entity.HomeCardsEntity

data class HomeCardsWithCountry(
    val card: List<HomeCardsEntity>,
    val countryCode: String?
)
