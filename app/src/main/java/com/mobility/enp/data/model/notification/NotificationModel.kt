package com.mobility.enp.data.model.notification

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "NotificationsTable")
data class NotificationModel(
    @PrimaryKey(true)
    val id: Int?,
    val title: String?,
    val description: String?,
    val time: Long
) {
    constructor(title: String?, description: String?, time: Long) : this(
        null,
        title,
        description,
        time
    )
}