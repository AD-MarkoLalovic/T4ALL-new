package com.mobility.enp.data.model.api_tool_history.v2base_model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.google.gson.annotations.Expose

@Keep
data class Objection(
    @SerializedName("complaint_request_id")
    @Expose
    val complaintRequestId: Int?,
    @SerializedName("created_at")
    @Expose
    val createdAt: String?,
    @SerializedName("customer_id")
    @Expose
    val customerId: Int?,
    @SerializedName("id")
    @Expose
    val id: Int?,
    @SerializedName("item_date")
    @Expose
    val itemDate: String?,
    @SerializedName("item_number")
    @Expose
    val itemNumber: String?,
    @SerializedName("item_options")
    @Expose
    val itemOptions: String?,
    @SerializedName("item_reason")
    @Expose
    val itemReason: String?,
    @SerializedName("updated_at")
    @Expose
    val updatedAt: String?
)