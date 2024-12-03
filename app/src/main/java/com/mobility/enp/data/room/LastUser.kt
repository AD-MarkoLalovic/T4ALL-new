package com.mobility.enp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "last_user")
data class LastUser(
    @PrimaryKey(true) val id: Int, val email: String
) {
    constructor(email: String) : this(0, email)
}
