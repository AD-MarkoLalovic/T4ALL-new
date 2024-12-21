package com.mobility.enp.data.model.api_tool_history.listing

import androidx.annotation.Keep

@Keep
data class Complaint(
    val id: Int,
    val complaintText: String,
    val objections: List<Objections>?
)
