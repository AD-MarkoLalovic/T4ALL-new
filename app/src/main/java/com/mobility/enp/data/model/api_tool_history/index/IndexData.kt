package com.mobility.enp.data.model.api_tool_history.index


import androidx.annotation.Keep
import androidx.room.Entity
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "HISTORY_V2_TAGS",
    primaryKeys = ["currentPage", "lastPage"]
)
@Keep
data class IndexData(
    @SerializedName("data")
    @Expose
    val `data`: Data? = Data(),
    @SerializedName("message")
    @Expose
    val message: String? = "",
    var availableCountries: List<String>?,
    var currentPage: Int = 0,
    var lastPage: Int = 0,
    var totalRecords: Int = 0
){
    fun setPages(currentPage: Int,
                 lastPage: Int,
                 totalRecords: Int, availableCountries: List<String>){
        this.currentPage = currentPage
        this.lastPage = lastPage
        this.totalRecords = totalRecords
        this.availableCountries = availableCountries
    }
}
