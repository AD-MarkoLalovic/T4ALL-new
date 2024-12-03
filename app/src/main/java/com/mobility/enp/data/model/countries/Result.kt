package com.mobility.enp.data.model.countries


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.google.gson.annotations.Expose

@Keep
data class Result(
    @SerializedName("code")
    @Expose
    var code: String? = "",
    @SerializedName("id")
    @Expose
    var id: String? = ""
)