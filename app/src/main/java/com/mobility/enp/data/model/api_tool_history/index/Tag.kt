package com.mobility.enp.data.model.api_tool_history.index


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Tag(
    @SerializedName("category")
    var category: Category?,
    @SerializedName("registration_plate")
    var registrationPlate: String?,
    @SerializedName("serial_number")
    var serialNumber: String?
)