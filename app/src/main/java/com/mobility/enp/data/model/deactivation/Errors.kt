package com.mobility.enp.data.model.deactivation


import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
data class Errors(
    @SerializedName("email")
    @Expose
    var email: List<String?>?
)