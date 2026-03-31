package com.mobility.enp.data.model.new_toll_history.response

import com.google.gson.annotations.SerializedName

data class Objection(
    @SerializedName("complaint_request_id")
    val complaintRequestId: Int?,

    @SerializedName("created_at")
    val createdAt: String?,

    @SerializedName("customer_id")
    val customerId: Int?,

    @SerializedName("id")
    val id: Int?,

    @SerializedName("item_date")
    val itemDate: String?,

    @SerializedName("item_number")
    val itemNumber: String?,

    @SerializedName("item_options")
    val itemOptions: String?,

    @SerializedName("item_reason")
    val itemReason: String?,

    @SerializedName("updated_at")
    val updatedAt: String?
)