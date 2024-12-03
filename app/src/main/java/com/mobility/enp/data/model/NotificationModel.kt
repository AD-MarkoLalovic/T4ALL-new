package com.mobility.enp.data.model

import androidx.annotation.Keep

@Keep
data class NotificationModel(
    val text: String?,
    val image: Int?,
    val date: String?,
    val showElement: Boolean?
)