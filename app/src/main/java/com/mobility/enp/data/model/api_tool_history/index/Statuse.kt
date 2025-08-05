package com.mobility.enp.data.model.api_tool_history.index


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.google.gson.annotations.Expose

@Keep
data class Statuse(
    @SerializedName("country")
    @Expose
    val country: Country? = Country(),
    @SerializedName("status")
    @Expose
    val status: Status? = Status()
)