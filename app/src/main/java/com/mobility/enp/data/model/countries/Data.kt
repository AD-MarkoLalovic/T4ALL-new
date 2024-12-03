package com.mobility.enp.data.model.countries


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.google.gson.annotations.Expose

@Keep
data class Data(
    @SerializedName("results")
    @Expose
    var results: List<Result?>? = listOf()
)