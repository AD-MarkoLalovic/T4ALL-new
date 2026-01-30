package com.mobility.enp.data.model.api_tool_history.v2base_model


import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
@Entity(
    tableName = "HISTORY_V2_Montenegro",
    primaryKeys = ["serial", "countryCode"]
) // composite PK 
data class V2HistoryTagResponseMontenegro(
    val id: Int,
    @SerializedName("data")
    @Expose
    val `data`: Data?,
    @SerializedName("message")
    @Expose
    val message: String?,
    var serial: String,
    var countryCode: String
)