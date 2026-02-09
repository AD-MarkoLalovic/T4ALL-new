package com.mobility.enp.data.model.api_tool_history.v2base_model


import androidx.annotation.Keep
import androidx.room.Entity
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
@Entity(
    tableName = "HISTORY_V2_Serbia",
    primaryKeys = ["serial"]
) // composite PK 
data class V2HistoryTagResponseSerbia(
    @SerializedName("data")
    @Expose
    val `data`: Data?,
    @SerializedName("message")
    @Expose
    val message: String?,
    var serial: String,
    var countryCode: String
)