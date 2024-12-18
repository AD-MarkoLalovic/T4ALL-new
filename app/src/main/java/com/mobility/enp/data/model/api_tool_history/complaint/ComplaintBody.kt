package com.mobility.enp.data.model.api_tool_history.complaint

import androidx.annotation.Keep

@Keep
data class ComplaintBody(
    val itemId: Int,  // complaint id from user history
    val complaintText: String?,   // user text
    val complaintBankName: Int?, // id bank from api
    val complaintRegistration: String?,  // licence plate
    val accountZr: String?,  // bank left
    val accountZr2: String?,  // bank center
    val accountZr3: String?,  // bank right
)
