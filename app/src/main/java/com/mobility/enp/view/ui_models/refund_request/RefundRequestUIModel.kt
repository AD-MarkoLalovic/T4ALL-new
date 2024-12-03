package com.mobility.enp.view.ui_models.refund_request

data class RefundRequestUIModel(
    val id: Int,
    val serialNumber: String,
    val displayAmount: String,
    val registrationPlate: String,
    val statusText: String,
    val statusValue: Int,
    val bankAccount: String,
    val bank: String
)
