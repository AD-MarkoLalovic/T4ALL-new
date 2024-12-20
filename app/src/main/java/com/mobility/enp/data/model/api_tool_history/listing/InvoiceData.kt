package com.mobility.enp.data.model.api_tool_history.listing

import androidx.annotation.Keep

@Keep
data class InvoiceData(
    val items: List<InvoiceItem>,
    val sum: List<InvoiceSum>,
    val total: Int,
    val currentPage: Int,
    val perPage: Int,
    val lastPage: Int
)