package com.mobility.enp.data.model.api_room_models

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "loginTable")
data class UserLoginResponseRoomTable(
    @PrimaryKey(autoGenerate = true) // no need to pass this room will handle it
    val userId: Int? = null,  // do not provide the value
    val accessToken: String?,
    val tokenType: String?,
    val username: String?,
    val portalKey: String?
)
