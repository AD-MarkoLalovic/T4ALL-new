package com.mobility.enp.util

import android.content.Context
import com.mobility.enp.R
import com.mobility.enp.data.model.home.cards.added_cards.entity.AddedCardsEntity
import com.mobility.enp.data.model.home.cards.added_cards.response.CardsList
import com.mobility.enp.data.model.home.cards.entity.HomeCardsEntity
import com.mobility.enp.data.model.home.cards.response.CardsHome
import com.mobility.enp.data.model.home.entity.TollHistoryHomeEntity
import com.mobility.enp.view.ui_models.home.HomeTollHistoryUI

fun TollHistoryHomeEntity.toUIModel(): HomeTollHistoryUI {
    return HomeTollHistoryUI(
        invoiceNumber = invoiceNumber,
        status = status,
        entryToll = entryToll,
        exitToll = exitToll,
        entryDataAndTime = "$entryDate $entryTime",
        exitDateAndTime = "$exitDate $exitTime",
        paymentAmount = paymentAmount,
        paymentCurrency = paymentCurrency
    )
}

/**
 * Konverzija responsa u entity za home card
 */
fun CardsHome.toEntityList(context: Context): List<HomeCardsEntity> {
    return results.map { result ->
        HomeCardsEntity(
            id = result.id,
            code = result.code,
            title = when (result.code) {
                "RS" -> context.getString(R.string.serbian_passage)
                "MK" -> context.getString(R.string.north_macedonian_passage)
                "ME" -> context.getString(R.string.montenegro_passage)
                else -> ""
            },
            description = when (result.code) {
                "RS" -> context.getString(R.string.tag_device_payment_method_serbia)
                "MK" -> context.getString(R.string.tag_device_payment_method_north_macedonia)
                "ME" -> context.getString(R.string.tag_device_payment_method_montenegro)
                else -> ""
            }
        )
    }
}

/**
 * Konverzija responsa za dodate kartice
 */
fun CardsList.toEntityAddedCards(): AddedCardsEntity? {
    return if (id != null && country?.code != null) {
        AddedCardsEntity(
            id = id,
            countryCode = country.code
        )
    } else {
        null
    }
}

