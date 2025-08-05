package com.mobility.enp.data.model.countries


import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
data class CountriesModel(
    @SerializedName("data")
    @Expose
    var `data`: Data? = Data(),
    @SerializedName("message")
    @Expose
    var message: String? = ""
)