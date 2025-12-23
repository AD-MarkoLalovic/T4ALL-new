package com.mobility.enp.data.model.api_tool_history.index


import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "indexdata")
@Keep
data class IndexData(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @SerializedName("data")
    @Expose
    val `data`: Data? = Data(),
    @SerializedName("message")
    @Expose
    val message: String? = "",
    var availableCountries: List<String>?
)
