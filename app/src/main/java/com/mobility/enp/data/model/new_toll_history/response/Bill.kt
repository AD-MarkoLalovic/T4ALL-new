package com.mobility.enp.data.model.new_toll_history.response

import com.google.gson.annotations.SerializedName

data class Bill(
    @SerializedName("bill_final")
    val billFinal: String?,
    @SerializedName("country_code")
    val countryCode: String?,
    val currency: String?,
    val discount: String?,
    @SerializedName("franchiser_id")
    val franchiserId: Any?,
    val id: String?,
    val paid: String?
)