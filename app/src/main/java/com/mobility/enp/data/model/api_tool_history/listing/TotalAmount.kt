package com.mobility.enp.data.model.api_tool_history.listing

import androidx.annotation.Keep

@Keep
data class TotalAmount(
    val total: String,
    val currency: String
)