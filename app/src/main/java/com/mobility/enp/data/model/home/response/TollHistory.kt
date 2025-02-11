package com.mobility.enp.data.model.home.response

data class TollHistory(
    val invoiceNumber: String,
    val status: Status?,
    val entryToll: String?,
    val exitToll: String?,
    val entryDate: String?,
    val exitDate: String?,
    val entryTime: String?,
    val exitTime: String?,
    val paymentAmount: PaymentAmount?,

)