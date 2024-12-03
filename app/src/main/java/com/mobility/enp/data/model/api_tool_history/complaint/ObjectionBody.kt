package com.mobility.enp.data.model.api_tool_history.complaint

import androidx.annotation.Keep
import java.math.BigInteger

@Keep
data class ObjectionBody(
    val complaintRequestId: Int,
    val objectionItemNumber: BigInteger,
    val objectionItemDate: String,
    val objectionItemOptions: String,
    val objectionItemReason: String
)
