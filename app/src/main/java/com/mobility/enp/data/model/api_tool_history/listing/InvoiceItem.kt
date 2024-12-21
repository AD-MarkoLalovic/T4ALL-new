package com.mobility.enp.data.model.api_tool_history.listing

import androidx.annotation.Keep

@Keep
data class InvoiceItem(
    val serialNumber: String,
    val transitItems: List<InvoiceRelation>
)
