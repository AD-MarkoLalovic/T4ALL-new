package com.mobility.enp.data.model.home.response

data class TotalCurrency(
    val currency: Currency,
    val isPaid: Boolean,
    val total: String
)