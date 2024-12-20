package com.mobility.enp.data.model.api_tool_history.listing

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
