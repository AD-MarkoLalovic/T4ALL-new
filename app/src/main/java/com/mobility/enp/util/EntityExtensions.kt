package com.mobility.enp.util

import android.content.Context
import com.mobility.enp.R
import com.mobility.enp.data.model.cards.registration_croatia.RegistrationResponse
import com.mobility.enp.data.model.cards.tags_for_croatia.Tag
import com.mobility.enp.data.model.cardsweb.CardWebModel
import com.mobility.enp.data.model.home.cards.entity.HomeCardsEntity
import com.mobility.enp.data.model.home.entity.TollHistoryHomeEntity
import com.mobility.enp.data.model.my_tags.response.MyTagsList
import com.mobility.enp.view.ui_models.TagsForCroatiaUI
import com.mobility.enp.view.ui_models.home.HomeTollHistoryUI
import com.mobility.enp.view.ui_models.my_tags.TagStatusUiModel
import com.mobility.enp.view.ui_models.my_tags.TagUiModel

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
fun CardWebModel.toEntityList(context: Context, user: String): List<HomeCardsEntity> {
    val listCards = mutableListOf<HomeCardsEntity>()

    if (data?.showTabRS == true && data.cardsRS.isNullOrEmpty()) {
        val srCard = HomeCardsEntity(
            email = user,
            code = "RS",
            title = context.getString(R.string.serbian_passage),
            description = context.getString(R.string.tag_device_payment_method_serbia),
            additionEnabled = data.hasSerbianCard,
        )
        listCards.add(srCard)
    }
    if (data?.showTabME == true && data.cardsME.isNullOrEmpty()) {
        val meCard = HomeCardsEntity(
            email = user,
            code = "ME",
            title = context.getString(R.string.montenegro_passage),
            description = context.getString(R.string.tag_device_payment_method_montenegro),
            additionEnabled = data.hasSerbianCard,
        )
        listCards.add(meCard)
    }
    if (data?.showTabMK == true && data.cardsMK.isNullOrEmpty()) {
        val mkCard = HomeCardsEntity(
            email = user,
            code = "MK",
            title = context.getString(R.string.north_macedonian_passage),
            description = context.getString(R.string.tag_device_payment_method_north_macedonia),
            additionEnabled = data.hasSerbianCard,
        )
        listCards.add(mkCard)
    }

    return listCards
}

fun List<Tag>.toTagsForCroatiaUIList(): List<TagsForCroatiaUI> {
    return this.map { tag ->
        TagsForCroatiaUI(
            serialNumberUI = tag.serialNumber,
            registrationPlateUI = tag.registrationPlate
        )
    }
}

fun List<MyTagsList>.toTagUiModel(): List<TagUiModel> {
    return this.map { tag ->
        TagUiModel(
            serialNumber = tag.serialNumber,
            registrationPlate = tag.registrationPlate,
            countryCode = tag.country?.value,
            statuses = tag.statuses.map {
                TagStatusUiModel(
                    statusesCountry = it.country?.text,
                    statusText = it.status?.text,
                    statusValue = it.status?.value
                )
            },
            showButtonFoundTag = tag.showButtonFoundTag,
            showButtonLostTag = tag.showButtonLostTag,
            category = tag.category?.value
        )
    }
}

fun RegistrationResponse.getRedirectWithToken(): String {
    return "${this.data.redirectUrl}/${this.data.token}"
}
