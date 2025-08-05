package com.mobility.enp.data.model.api_tool_history.v2base_model


import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
data class Tag(
    @SerializedName("serial_number")
    @Expose
    val serialNumber: String?,
    @SerializedName("serial_registration")
    @Expose
    val serialRegistration: String?
)