package com.mobility.enp.data.model.new_toll_history.mapper

import com.mobility.enp.data.model.new_toll_history.local.entity.AllowedCountryEntity
import com.mobility.enp.data.model.new_toll_history.local.entity.SumTagEntity
import com.mobility.enp.data.model.new_toll_history.local.entity.TollHistoryItemEntity
import com.mobility.enp.data.model.new_toll_history.remote.dto.AllowedCountry
import com.mobility.enp.data.model.new_toll_history.remote.dto.Item
import com.mobility.enp.data.model.new_toll_history.remote.dto.SumTag
import com.mobility.enp.data.model.new_toll_history.remote.dto.TollHistoryDto
import com.mobility.enp.view.ui_models.toll_history.AllowedCountryUi
import com.mobility.enp.view.ui_models.toll_history.SumTagUi
import com.mobility.enp.view.ui_models.toll_history.TollHistoryItemUi
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.Instant

private val apiDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

private val displayDataFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm", Locale.ENGLISH)

fun String?.toEpochMillis(): Long? {
    if (this.isNullOrBlank()) return null
    return try {
        LocalDateTime.parse(this, apiDateFormatter)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    } catch (e: Exception) {
        null
    }
}

fun Long?.toDisplayDate(): String {
    if (this == null) return ""
    return try {
        val dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(this),
            ZoneId.systemDefault()
        )
        dateTime.format(displayDataFormatter)
    } catch (e: Exception) {
        ""
    }
}

data class TollHistoryMappedData(
    val countries: List<AllowedCountryEntity>,
    val sumTags: List<SumTagEntity>,
    val item: List<TollHistoryItemEntity>
)

fun TollHistoryDto.toMappedData(filterCountry: String): TollHistoryMappedData {
    val countries = data?.allowedCountries?.filterNotNull()
        ?.mapIndexed { index, country -> country.totoEntity(index) } ?: emptyList()
    val sumTags = data?.sumTags?.filterNotNull()?.mapIndexed { index, tag -> tag.toEntity(index) }
        ?: emptyList()
    val items =
        data?.records?.items?.filterNotNull()?.map { it.totoEntity(filterCountry) } ?: emptyList()

    return TollHistoryMappedData(countries, sumTags, items)
}

fun AllowedCountry.totoEntity(positon: Int): AllowedCountryEntity {
    return AllowedCountryEntity(
        value = value ?: "",
        name = name ?: "",
        position = positon
    )
}

fun AllowedCountryEntity.toUi(isSelected: Boolean): AllowedCountryUi {
    return AllowedCountryUi(
        value = value,
        name = name,
        position = position,
        isSelected = isSelected
    )
}

fun SumTag.toEntity(positon: Int): SumTagEntity {
    return SumTagEntity(
        tagSerialNumber = tagSerialNumber ?: "",
        currency = currency ?: "",
        total = total ?: "",
        position = positon
    )
}

fun SumTagEntity.toUi(): SumTagUi {
    return SumTagUi(
        tagSerialNumber = tagSerialNumber,
        totalDisplay = total,
        currencyDisplay = currency
    )
}

fun Item.totoEntity(filterCountry: String): TollHistoryItemEntity {
    return TollHistoryItemEntity(
        id = id ?: 0,
        tagsSerialNumber = tagsSerialNumberSame ?: "",
        tollPlaza = tollPlaza ?: "",
        isPaid = isPaid ?: false,
        checkInDate = checkInDate.toEpochMillis(),
        checkOutDate = checkOutDate.toEpochMillis(),
        amountWithOutDiscount = amountWithOutDiscount ?: "",
        currency = currency ?: "",
        billFinal = bill?.billFinal ?: "",
        complaintId = complaint?.id,
        objectionCount = complaint?.objections?.filterNotNull()?.size ?: 0,
        filterCountry = filterCountry
    )
}

fun TollHistoryItemEntity.toUi(): TollHistoryItemUi {
    return TollHistoryItemUi(
        id = id,
        billFinal = billFinal,
        amountDisplay = amountWithOutDiscount,
        currencyDisplay = currency,
        tollPlaza = tollPlaza,
        checkInFormatted = checkInDate.toDisplayDate(),
        checkOutFormatted = checkOutDate.toDisplayDate(),
        isPaid = isPaid,
        complaintId = complaintId,
        objectionCount = objectionCount,
        maxObjectionsReached = objectionCount >= 2
    )
}


































