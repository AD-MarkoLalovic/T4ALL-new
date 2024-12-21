package com.mobility.enp.data.model.api_tool_history.listing

import androidx.annotation.Keep

@Keep
data class InvoiceSum(
    val serialNumber: String,
    val total: List<TotalAmount>
)