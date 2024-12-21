package com.mobility.enp.data.model.api_tool_history.listing

import androidx.annotation.Keep

@Keep
data class InvoiceRelation(
    val itemId: Int,
    val invoiceNumber: String,
    val status: InvoiceStatus,
    var entryToll: String,
    val exitToll: String,
    val entryDate: String,
    val exitDate: String,
    val entryTime: String,
    val exitTime: String,
    val complaint: Complaint?,
    val amount: String,
    val currency: String,
)