package com.mobility.enp.data.model.csv_table


import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
data class Data(
    @SerializedName("csvContent")
    @Expose
    var csvContent: String? = "",
    @SerializedName("status")
    @Expose
    var status: String? = ""
)