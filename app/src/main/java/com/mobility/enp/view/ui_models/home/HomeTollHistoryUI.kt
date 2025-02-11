package com.mobility.enp.view.ui_models.home

data class HomeTollHistoryUI(
    val invoiceNumber: String,
    val entryToll: String?,
    val exitToll: String?,
    val entryDataAndTime: String?,
    val exitDateAndTime: String?,
    val payment: String
)