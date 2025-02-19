package com.mobility.enp.view.ui_models.home

data class HomeTollHistoryUI(
    val invoiceNumber: String,
    val status: Int?,
    val entryToll: String?,
    val exitToll: String?,
    val entryDataAndTime: String?,
    val exitDateAndTime: String?,
    val paymentAmount: String?,
    val paymentCurrency: String?
)