package com.mobility.enp.data.model.countries


import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
data class Data(
    @SerializedName("results")
    @Expose
    var results: List<Result?>? = listOf()
)