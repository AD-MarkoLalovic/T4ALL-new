package com.mobility.enp.data.model.api_tool_history.v2base_model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.google.gson.annotations.Expose

@Keep
data class Data(
    @SerializedName("allowedCountries")
    @Expose
    val allowedCountries: List<AllowedCountry?>?,
    @SerializedName("customer")
    @Expose
    val customer: Customer?,
    @SerializedName("records")
    @Expose
    val records: Records?,
    @SerializedName("sumTags")
    @Expose
    val sumTags: List<SumTag?>?,
    @SerializedName("tags")
    @Expose
    val tags: List<Tag?>?
)