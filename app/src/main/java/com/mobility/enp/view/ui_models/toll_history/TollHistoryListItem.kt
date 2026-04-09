package com.mobility.enp.view.ui_models.toll_history

sealed interface TollHistoryListItem {

    data class TagHeader(
        val tagSerialNumber: String,
        val total: String,
        val currency: String
    ) : TollHistoryListItem

    data class PassageItem(
        val passage: TollHistoryItemUi
    ) : TollHistoryListItem

    data object GroupEnd : TollHistoryListItem

}