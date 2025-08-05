package com.mobility.enp.data.model.countries


import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
data class Result(
    @SerializedName("code")
    @Expose
    var code: String? = "",
    @SerializedName("id")
    @Expose
    var id: String? = ""
)