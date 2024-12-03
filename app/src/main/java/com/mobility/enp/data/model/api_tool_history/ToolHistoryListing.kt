package com.mobility.enp.data.model.api_tool_history

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity("historyListing")
data class ToolHistoryListing(
    @PrimaryKey(false)
    var serial: String,
    val data: InvoiceData,
    val message: String
)

@Keep
data class InvoiceData(
    val items: List<InvoiceItem>,
    val sum: List<InvoiceSum>,
    val total: Int,
    val currentPage: Int,
    val perPage: Int,
    val lastPage: Int
)

@Keep
data class InvoiceItem(
    val serialNumber: String,
    val transitItems: List<InvoiceRelation>
)

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

@Keep
data class Complaint(
    val id: Int,
    val complaintText: String,
    val objections: List<Objections>?
)

@Keep
data class Objections(
    val id: Int,
    val objectionText: String
)

@Keep
data class InvoiceStatus(
    val value: Int,
    val text: String
)

@Keep
data class InvoiceSum(
    val serialNumber: String,
    val total: List<TotalAmount>
)

@Keep
data class TotalAmount(
    val total: String,
    val currency: String
)
