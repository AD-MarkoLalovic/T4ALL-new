package com.mobility.enp.util

import com.mobility.enp.data.model.home.entity.TollHistoryHomeEntity
import com.mobility.enp.view.ui_models.home.HomeTollHistoryUI

fun TollHistoryHomeEntity.toUIModel(): HomeTollHistoryUI {
    return HomeTollHistoryUI(
        invoiceNumber = invoiceNumber,
        status = status,
        entryToll = entryToll,
        exitToll =  exitToll,
        entryDataAndTime = "$entryDate $entryTime",
        exitDateAndTime = "$exitDate $exitTime",
        paymentAmount = paymentAmount,
        paymentCurrency = paymentCurrency
    )
}
