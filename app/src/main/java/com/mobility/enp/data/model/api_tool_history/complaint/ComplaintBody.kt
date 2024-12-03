package com.mobility.enp.data.model.api_tool_history.complaint

import androidx.annotation.Keep

@Keep
data class ComplaintBody(
    val itemId: Int,
    val complaintText: String,
    val complaintAccountNumber: String,
    val complaintBankName: String,
    val complaintRegistration: String,
)
