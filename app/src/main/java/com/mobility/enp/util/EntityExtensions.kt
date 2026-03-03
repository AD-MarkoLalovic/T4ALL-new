package com.mobility.enp.util

import android.content.Context
import com.mobility.enp.R
import com.mobility.enp.data.model.api_my_profile.my_tags.response.MyTagsList
import com.mobility.enp.data.model.api_tool_history.v2base_model.Item
import com.mobility.enp.data.model.api_tool_history.v2base_model.SumTag
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponse
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponseCroatia
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponseCroatiaResult
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponseResult
import com.mobility.enp.data.model.cards.registration_croatia.RegistrationResponse
import com.mobility.enp.data.model.cards.tags_for_croatia.Tag
import com.mobility.enp.data.model.cardsweb.CardWebModel
import com.mobility.enp.data.model.home.cards.entity.HomeCardsEntity
import com.mobility.enp.data.model.home.entity.TollHistoryHomeEntity
import com.mobility.enp.view.ui_models.TagsForCroatiaUI
import com.mobility.enp.view.ui_models.home.HomeTollHistoryUI
import com.mobility.enp.view.ui_models.my_tags.TagStatusUiModel
import com.mobility.enp.view.ui_models.my_tags.TagUiModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

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

    // added 2 general objects for social networks

    val facebookCard = HomeCardsEntity(
        email = user,
        code = "facebook",
        title = context.getString(R.string.socialNetworksTitle),
        description = context.getString(R.string.facebook_text),
        additionEnabled = false,
        isSocialNetworks = true
    )

    listCards.add(facebookCard)

    val instagramCard = HomeCardsEntity(
        email = user,
        code = "instagram",
        title = context.getString(R.string.socialNetworksTitle),
        description = context.getString(R.string.instagram_text),
        additionEnabled = false,
        isSocialNetworks = true
    )

    listCards.add(instagramCard)

    return listCards
}

fun Date.toLocalDate(): LocalDate =
    this.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

fun List<Tag>.toTagsForCroatiaUIList(): List<TagsForCroatiaUI> {
    return this.map { tag ->
        val statusValueForCroatia = tag.statuses
            ?.firstOrNull { it.country?.value == "HR" }
            ?.status
            ?.value

        TagsForCroatiaUI(
            serialNumberUI = tag.serialNumber,
            registrationPlateUI = tag.registrationPlate,
            status = statusValueForCroatia
        )
    }
}


fun V2HistoryTagResponse.toCroatianPassage(): V2HistoryTagResponseCroatia {
    return V2HistoryTagResponseCroatia(
        data, message, serial, countryCode,
        data?.records?.pagination?.currentPage ?: 0,
        data?.records?.pagination?.lastPage ?: 0,
        data?.records?.pagination?.total ?: 0,
        data?.records?.pagination?.perPage ?: 0
    )
}


fun V2HistoryTagResponse.toCroatianPassageResult(): V2HistoryTagResponseCroatiaResult {
    return V2HistoryTagResponseCroatiaResult(
        data, message, serial, countryCode,
        data?.records?.pagination?.currentPage ?: 0,
        data?.records?.pagination?.lastPage ?: 0,
        data?.records?.pagination?.total ?: 0,
        data?.records?.pagination?.perPage ?: 0
    )
}


fun V2HistoryTagResponse.toV2HistoryTagResponseResult(): V2HistoryTagResponseResult {
    return V2HistoryTagResponseResult(
        data, message, serial, countryCode,
        data?.records?.pagination?.currentPage ?: 0,
        data?.records?.pagination?.lastPage ?: 0,
        data?.records?.pagination?.total ?: 0,
        data?.records?.pagination?.perPage ?: 0
    )
}


fun V2HistoryTagResponseCroatia.toV2Response(): V2HistoryTagResponse {
    return V2HistoryTagResponse(
        data, message, serial, countryCode,
        currentPage,
        lastPage,
        totalRecords,
        perPage
    )
}

fun List<MyTagsList>.toTagUiModel(): List<TagUiModel> {
    return this.map { tag ->
        TagUiModel(
            serialNumber = tag.serialNumber,
            registrationPlate = tag.registrationPlate,
            countryCode = tag.country?.value,
            countryName = tag.country?.text,
            statuses = tag.statuses?.map {
                TagStatusUiModel(
                    statusesCountry = it.country?.value,
                    statusText = it.status?.text,
                    statusValue = it.status?.value
                )
            } ?: emptyList(),
            showButtonFoundTag = tag.showButtonFoundTag,
            showButtonLostTag = tag.showButtonLostTag,
            category = tag.category?.value,
            franchiser = tag.franchiser?.companyName,
            showButtonActivateTag = tag.showButtonActivateTag,
            showButtonDeactivateTag = tag.showButtonDeactivateTag
        )
    }
}

fun List<Item>.toSumTagsByCurrency(): List<SumTag> {
    val symbols = DecimalFormatSymbols(Locale("sr", "RS")).apply {
        decimalSeparator = ','
        groupingSeparator = '.'
    }
    val formatter = DecimalFormat("#,##0.00", symbols)

    return this
        .groupBy { it.currency?.uppercase() ?: "UNKNOWN" }
        .map { (currency, itemsInCurrency) ->

            val total = itemsInCurrency.sumOf { item ->
                item.amount ?: 0.0
            }

            SumTag(
                currency = currency,
                tagSerialNumber = itemsInCurrency.firstOrNull()?.tagsSerialNumber,
                total = formatter.format(total)
            )
        }
}

fun RegistrationResponse.getRedirectWithToken(): String {
    return "${this.data.redirectUrl}/${this.data.token}"
}
