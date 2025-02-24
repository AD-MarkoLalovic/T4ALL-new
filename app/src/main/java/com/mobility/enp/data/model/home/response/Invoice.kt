package com.mobility.enp.data.model.home.response

data class  Invoice(
    val month: Month,
    val totalCurrency: List<TotalCurrency>,
    val year: String
)