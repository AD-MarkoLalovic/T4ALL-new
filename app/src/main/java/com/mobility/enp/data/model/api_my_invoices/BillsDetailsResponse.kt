package com.mobility.enp.data.model.api_my_invoices

import androidx.annotation.Keep

@Keep
data class Bill(
    val id: String?,
    val billFinal: String?,
    val total: String?,
    val currency: BillsDetailsCurrency?,
    val country: Country?,
    val dateOfIssue: String?,
    val datePaid: String?,
    val status: Status?,
    val discount: Discount?
)

@Keep
data class BillsDetailsCurrency(
    val value: String?,
    val text: String?
)

@Keep
data class Country(
    val value: String?,
    val text: String?
)

@Keep
data class Status(
    val value: Int,
    val text: String
)

@Keep
data class Discount(
    val main: String,
    val secondary: String
)

@Keep
data class BillData(
    val bills: List<Bill>,
    val total: Int,
    val currentPage: Int,
    val perPage: Int,
    val lastPage: Int
)

@Keep
data class BillsDetailsResponse(
    val data: BillData,
    val message: String
)
