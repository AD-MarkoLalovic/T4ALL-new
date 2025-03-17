package com.mobility.enp.data.model.api_room_models

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@Entity(tableName = "loginTable")
data class UserLoginResponseRoomTable(
    @PrimaryKey(autoGenerate = true) // no need to pass this room will handle it
    val userId: Int? = null,  // do not provide the value
    val accessToken: String?,
    val tokenType: String?,
    val message: String?,
    val username: String?,
    val password: String?,
) : Parcelable {
    constructor(
        accessToken: String?,
        tokenType: String?,
        message: String?,
        username: String?,
        password: String?
    ) : this(null, accessToken, tokenType, message, username, password)
}
