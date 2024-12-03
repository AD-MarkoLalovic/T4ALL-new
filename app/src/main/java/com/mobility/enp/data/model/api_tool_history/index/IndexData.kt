package com.mobility.enp.data.model.api_tool_history.index


import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Keep
@Entity("IndexData")
data class IndexData(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @SerializedName("data")
    var `data`: Data?,
    @SerializedName("message")
    var message: String?
)