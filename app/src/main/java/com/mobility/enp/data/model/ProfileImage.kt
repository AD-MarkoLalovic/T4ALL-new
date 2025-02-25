package com.mobility.enp.data.model

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "profile_image")
data class ProfileImage(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val displayName: String,
    @ColumnInfo(name = "image_path") val imagePath: String
)
