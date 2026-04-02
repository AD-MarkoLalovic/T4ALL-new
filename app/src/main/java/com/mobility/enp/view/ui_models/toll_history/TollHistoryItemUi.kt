package com.mobility.enp.view.ui_models.toll_history

data class TollHistoryItemUi(
    val id: Int,
    val billFinal: String,
    val amountDisplay: String,
    val currencyDisplay: String,
    val tollPlaza: String,
    val checkInFormatted: String,
    val checkOutFormatted: String,
    val isPaid: Boolean,
    val complaintId: Int?,
    val objectionCount: Int,
    val maxObjectionsReached: Boolean
)
