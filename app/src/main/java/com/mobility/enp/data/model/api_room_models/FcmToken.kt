package com.mobility.enp.data.model.api_room_models

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fcm_token")
@Keep
data class FcmToken(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,
    val fcm_token: String?  // ignore recommendation
) {
    constructor(fcmToken: String) : this(null, fcmToken)
}
