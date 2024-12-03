package com.mobility.enp.data.model.countries


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.google.gson.annotations.Expose

@Keep
data class CountriesModel(
    @SerializedName("data")
    @Expose
    var `data`: Data? = Data(),
    @SerializedName("message")
    @Expose
    var message: String? = ""
)