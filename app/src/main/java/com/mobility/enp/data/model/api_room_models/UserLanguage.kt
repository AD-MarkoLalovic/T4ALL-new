package com.mobility.enp.data.model.api_room_models

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "languageTable")
@Keep
data class UserLanguage(
    @PrimaryKey(true)
    var id: Int?,
    var userLanguage: String?
) {
    constructor(userLanguage: String?) : this(null, userLanguage)
}
