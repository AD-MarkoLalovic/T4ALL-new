package com.mobility.enp.data.model.api_tool_history.v2base_model


import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
data class Item(
    @SerializedName("amount")
    @Expose
    var amount: Double?,
    @SerializedName("amountWithDiscount")
    @Expose
    val amountWithDiscount: String?,
    @SerializedName("amountWithOutDiscount")
    @Expose
    val amountWithOutDiscount: String?,
    @SerializedName("bill")
    @Expose
    val bill: Bill,
    @SerializedName("checkDate")
    @Expose
    val checkDate: String?,
    @SerializedName("check_in_date")
    @Expose
    var checkInDate: String?,
    @SerializedName("check_in_toll_plaza")
    @Expose
    val checkInTollPlaza: Int?,
    @SerializedName("check_out_date")
    @Expose
    var checkOutDate: String?,
    @SerializedName("check_out_toll_plaza")
    @Expose
    val checkOutTollPlaza: Int?,
    @SerializedName("complaint")
    @Expose
    val complaint: Complaint?,
    @SerializedName("currency")
    @Expose
    val currency: String?,
    @SerializedName("id")
    @Expose
    val id: Int,
    @SerializedName("isPaid")
    @Expose
    val isPaid: Boolean?,
    @SerializedName("paidLabel")
    @Expose
    val paidLabel: String?,
    @SerializedName("tagsSerialNumber")
    @Expose
    val tagsSerialNumber: String?,
    @SerializedName("tags_serial_number")
    @Expose
    val tags_serial_number: String?,
    @SerializedName("tollPlaza")
    @Expose
    val tollPlaza: String?
)