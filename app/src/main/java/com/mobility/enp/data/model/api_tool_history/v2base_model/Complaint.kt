package com.mobility.enp.data.model.api_tool_history.v2base_model


import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
data class Complaint(
    @SerializedName("id")
    @Expose
    val id: Int,
    @SerializedName("objections")
    @Expose
    val objections: List<Objection?>?
)