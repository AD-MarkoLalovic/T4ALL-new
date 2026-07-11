package com.mobility.enp.view.ui_models.toll_history

data class TollHistoryFilterTagUi(
    val serialNumber: String,
    val registrationPlate: String,
    val showSerialNumber: Boolean,
    val isSelected: Boolean,
    val isLastItem: Boolean = false,
    val categoryText: String? = null,
    val categoryValue: Int? = null
)
