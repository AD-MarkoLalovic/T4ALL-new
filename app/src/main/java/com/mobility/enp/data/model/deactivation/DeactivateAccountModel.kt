package com.mobility.enp.data.model.deactivation


import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
data class DeactivateAccountModel(
    @SerializedName("data")
    @Expose
    var `data`: Data?,
    @SerializedName("errors")
    @Expose
    var errors: Errors?,
    @SerializedName("message")
    @Expose
    var message: String?
)