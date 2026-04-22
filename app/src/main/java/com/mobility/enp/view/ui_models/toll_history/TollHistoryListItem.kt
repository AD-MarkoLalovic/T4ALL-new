package com.mobility.enp.view.ui_models.toll_history

sealed interface TollHistoryListItem {

    data class TagHeader(
        val tagSerialNumber: String,
        val total: String,
        val currency: String
    ) : TollHistoryListItem {
        val uniqueKey: String
            get() = "$currency-$tagSerialNumber"
    }

    data class PassageItem(
        val passage: TollHistoryItemUi
    ) : TollHistoryListItem

    data class GroupEnd(val currency: String, val afterTagSerialNumber: String) :
        TollHistoryListItem {
        val uniqueKey: String
            get() = "$currency-$afterTagSerialNumber"
    }

}