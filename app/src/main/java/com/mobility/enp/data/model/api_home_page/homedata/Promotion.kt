package com.mobility.enp.data.model.api_home_page.homedata

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Keep
@Entity(tableName = "promotions_table")
data class Promotion(
    @PrimaryKey(true)
    val id: Int,
    var title: String,
    var description: String,
    val priority: Int,
    val endDate: String,
    var countryCode: String?,
    var deletedByUser: Boolean?,
    var time: Long
) : Serializable {
    constructor(
        title: String,
        description: String,
        priority: Int,
        endDate: String,
        countryCode: String?,
        deletedByUser: Boolean?
    )
            : this(
        0,
        title,
        description,
        priority,
        endDate,
        countryCode,
        deletedByUser,
        System.currentTimeMillis()
    )
}