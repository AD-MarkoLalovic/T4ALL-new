package com.mobility.enp.data.model.api_my_invoices

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "monthly_bills")
data class MyInvoicesResponse(
    @PrimaryKey
    val id: Int,
    val data: DataMonthly,
    val message: String? = null
)

@Keep
data class DataMonthly(
    val months: List<Month>,
    val total: Int,
    val currentPage: Int,
    val perPage: Int,
    val lastPage: Int
)

@Keep
data class Month(
    val month: MonthInfo?,
    val year: String?,
    val totalCurrency: List<TotalCurrency>?
)

@Keep
data class MonthInfo(
    val name: String?,
    val value: String?
)

@Keep
data class TotalCurrency(
    val total: String?,
    val isPaid: Boolean?,
    val currency: Currency?
)

@Keep
data class Currency(
    val label: String?,
    val value: String?
)