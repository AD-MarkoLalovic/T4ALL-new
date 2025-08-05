package com.mobility.enp.data.model.api_tool_history.index


import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
data class Statuse(
    @SerializedName("country")
    @Expose
    val country: Country? = Country(),
    @SerializedName("status")
    @Expose
    val status: Status? = Status()
)