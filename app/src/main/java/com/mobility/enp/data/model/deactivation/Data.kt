package com.mobility.enp.data.model.deactivation


import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
data class Data(
    @SerializedName("data")
    @Expose
    var `data`: String?,
    @SerializedName("message")
    @Expose
    var message: String?
)