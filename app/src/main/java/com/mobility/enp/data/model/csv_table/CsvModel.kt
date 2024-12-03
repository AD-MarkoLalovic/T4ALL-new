package com.mobility.enp.data.model.csv_table


import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
data class CsvModel(
    @SerializedName("data")
    @Expose
    var `data`: Data? = Data(),
    @SerializedName("message")
    @Expose
    var message: String? = ""
)