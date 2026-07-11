package com.mobility.enp.view.ui_models.toll_history

data class TollHistoryFilterDateUi(
    val displayText: String,
    val apiFormat: String,
    val epochMillis: Long? = null
)
