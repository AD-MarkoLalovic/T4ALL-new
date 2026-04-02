package com.mobility.enp.data.model.new_toll_history.remote.dto

import com.google.gson.annotations.SerializedName

data class Item(
    val amount: Int?,
    val amountWithDiscount: String?,
    val amountWithOutDiscount: String?,
    val bill: Bill?,
    val checkDate: String?,
    @SerializedName("check_in_date")
    val checkInDate: String?,
    @SerializedName("check_in_toll_plaza")
    val checkInTollPlaza: Int?,
    @SerializedName("check_out_date")
    val checkOutDate: String?,
    @SerializedName("check_out_toll_plaza")
    val checkOutTollPlaza: Int?,
    val complaint: Complaint?,
    val currency: String?,
    @SerializedName("grand_total")
    val grandTotal: Double?,
    val id: Int?,
    val isPaid: Boolean?,
    val paidLabel: String?,
    val tagsSerialNumber: String?,
    @SerializedName("tags_serial_number")
    val tagsSerialNumberSame: String?,
    val tollPlaza: String?
)