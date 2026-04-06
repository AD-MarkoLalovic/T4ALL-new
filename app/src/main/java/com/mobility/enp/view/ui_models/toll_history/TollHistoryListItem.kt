package com.mobility.enp.view.ui_models.toll_history

sealed interface TollHistoryListItem {

    data class TagHeader(
        val serialNumber: String,
        val total: String,
        val currency: String
    ) : TollHistoryListItem

    data class PassageItem(
        val passage: TollHistoryItemUi
    )

}