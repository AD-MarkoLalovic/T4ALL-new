package com.mobility.enp.data.model.api_tool_history

import androidx.annotation.Keep
import java.util.Date

@Keep
data class TimeSave(
    val formattedTime: String,
    val inDateForm: Date?
)
