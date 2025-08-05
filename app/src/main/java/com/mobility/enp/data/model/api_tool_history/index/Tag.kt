package com.mobility.enp.data.model.api_tool_history.index


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.google.gson.annotations.Expose

@Keep
data class Tag(
    @SerializedName("category")
    @Expose
    val category: Category? = Category(),
    @SerializedName("country")
    @Expose
    val country: Country? = Country(),
    @SerializedName("id")
    @Expose
    val id: String? = "",
    @SerializedName("registrationPlate")
    @Expose
    var registrationPlate: String? = "",
    @SerializedName("roming")
    @Expose
    val roming: Boolean? = false,
    @SerializedName("serialNumber")
    @Expose
    val serialNumber: String? = "",
    @SerializedName("showButtonFoundTag")
    @Expose
    val showButtonFoundTag: Boolean? = false,
    @SerializedName("showButtonLostTag")
    @Expose
    val showButtonLostTag: Boolean? = false,
    @SerializedName("statuses")
    @Expose
    val statuses: List<Statuse>? = listOf()
)