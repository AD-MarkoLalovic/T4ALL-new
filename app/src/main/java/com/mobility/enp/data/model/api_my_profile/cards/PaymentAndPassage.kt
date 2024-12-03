package com.mobility.enp.data.model.api_my_profile.cards

import androidx.annotation.Keep

@Keep
data class PaymentAndPassage(
    val data: List<Card>?,
    val message: String? = null
)

@Keep
data class Card(
    val id: Int?,
    val cardDetails: String?,
    val cardType: String?,
    val country: Country?,
    val defaultCard: Int?,
    val active: Int?
)

@Keep
data class Country(
    val code: String?,
    val name: String?
)
