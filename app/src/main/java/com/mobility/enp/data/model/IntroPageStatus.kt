package com.mobility.enp.data.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "intro_page_status")
data class IntroPageStatus(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val key: String,
    val value: Boolean
) {
    constructor(key: String, value: Boolean) : this(null, key, value)
}