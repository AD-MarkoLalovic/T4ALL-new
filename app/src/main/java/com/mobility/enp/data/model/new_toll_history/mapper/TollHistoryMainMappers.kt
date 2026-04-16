package com.mobility.enp.data.model.new_toll_history.mapper

import android.content.Context
import com.mobility.enp.R
import com.mobility.enp.data.model.cardsweb.Data
import com.mobility.enp.data.model.new_toll_history.local.entity.AllowedCountryEntity
import com.mobility.enp.data.model.new_toll_history.local.entity.SumTagEntity
import com.mobility.enp.data.model.new_toll_history.local.entity.TollHistoryItemEntity
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
    //val countries: List<AllowedCountryEntity>,
    val sumTags: List<SumTagEntity>,
    val items: List<TollHistoryItemEntity>
)

fun TollHistoryDto.toMappedData(
    filterCountry: String,
    startSortIndex: Int = 0,
    page: Int = 1
): TollHistoryMappedData {
    val sumTags = data?.sumTags?.filterNotNull()
        ?.mapIndexed { index, tag -> tag.toEntity(index) } ?: emptyList()
    val items = toAllRows(filterCountry, startSortIndex, page)
    return TollHistoryMappedData(sumTags, items)
}

fun Data.countriesToEntity(): List<AllowedCountryEntity> {
    return buildList {
        if (showTabRS) add(AllowedCountryEntity("RS",size))
        if (showTabME) add(AllowedCountryEntity("ME",size))
        if (showTabMK) add(AllowedCountryEntity("MK",size))
        if (showTabHR) add(AllowedCountryEntity("HR",size))
    }
}

fun AllowedCountryEntity.toUi(isSelected: Boolean): AllowedCountryUi {
    return AllowedCountryUi(
        value = value,
        position = position,
        isSelected = isSelected
    )
}

fun String.toDisplayName(context: Context): String {
    return when (this) {
        "RS" -> context.getString(R.string.serbia)
        "ME" -> context.getString(R.string.montenegro)
        "MK" -> context.getString(R.string.macedonia)
        "HR" -> context.getString(R.string.croatia)
        else -> this
    }
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

/**
 * Sekcije u redosledu [sumTags] iz API odgovora; unutar grupe prolazi u redosledu kao u [items].
 * Prazne grupe (nema prolaza za taj tag na strani) se preskaču. Svaka stranica ima svoje total-e u sumTags.
 * Fallback: ako nema sumTags, redosled je kao u [items] (promena taga u nizu).
 */
fun TollHistoryDto.toAllRows(
    filterCountry: String,
    startSortIndex: Int = 0,
    page: Int = 1
): List<TollHistoryItemEntity> {
    val sumTagsOrdered = data?.sumTags?.filterNotNull() ?: emptyList()
    val ordered = data?.records?.items?.filterNotNull() ?: emptyList()
    if (ordered.isEmpty()) return emptyList()

    val sumTagMap: Map<String, SumTag> =
        sumTagsOrdered.associateBy { it.tagSerialNumber ?: "" }

    return if (sumTagsOrdered.isNotEmpty()) {
        buildRowsBySumTagsOrder(
            filterCountry, startSortIndex, page, sumTagsOrdered, ordered
        )
    } else {
        buildRowsByApiItemOrder(
            filterCountry, startSortIndex, page, ordered, sumTagMap
        )
    }
}

private fun buildRowsBySumTagsOrder(
    filterCountry: String,
    startSortIndex: Int,
    page: Int,
    sumTagsOrdered: List<SumTag>,
    ordered: List<Item>
): List<TollHistoryItemEntity> {
    val result = mutableListOf<TollHistoryItemEntity>()
    var sortIndex = startSortIndex
    var segmentIndex = 0

    for (sumTag in sumTagsOrdered) {
        val serial = sumTag.tagSerialNumber ?: ""
        if (serial.isEmpty()) continue

        val passagesForTag = ordered.filter { (it.tagsSerialNumberSame ?: "") == serial }
        if (passagesForTag.isEmpty()) continue

        segmentIndex++
        val tagTotal = sumTag.total ?: ""
        val tagCurrency = sumTag.currency ?: ""

        result.add(
            TollHistoryItemEntity(
                rowId = "H|$serial|$filterCountry|p$page|s$segmentIndex",
                rowType = TollHistoryItemEntity.ROW_TYPE_HEADER,
                sortIndex = sortIndex++,
                filterCountry = filterCountry,
                id = 0,
                tagsSerialNumber = serial,
                tollPlaza = "",
                isPaid = false,
                checkInDate = null,
                checkOutDate = null,
                amountWithOutDiscount = "",
                currency = "",
                billFinal = "",
                objectionCount = 0,
                complaintId = null,
                tagTotal = tagTotal,
                tagCurrency = tagCurrency
            )
        )

        for (item in passagesForTag) {
            result.add(
                passageEntity(
                    item = item,
                    serial = serial,
                    filterCountry = filterCountry,
                    sortIndex = sortIndex++,
                    tagTotal = tagTotal,
                    tagCurrency = tagCurrency
                )
            )
        }

        result.add(
            TollHistoryItemEntity(
                rowId = "G|$serial|$filterCountry|p$page|s$segmentIndex",
                rowType = TollHistoryItemEntity.ROW_TYPE_GROUP_END,
                sortIndex = sortIndex++,
                filterCountry = filterCountry,
                id = 0,
                tagsSerialNumber = serial,
                tollPlaza = "",
                isPaid = false,
                checkInDate = null,
                checkOutDate = null,
                amountWithOutDiscount = "",
                currency = "",
                billFinal = "",
                objectionCount = 0,
                complaintId = null,
                tagTotal = tagTotal,
                tagCurrency = tagCurrency
            )
        )
    }

    return result
}

private fun passageEntity(
    item: Item,
    serial: String,
    filterCountry: String,
    sortIndex: Int,
    tagTotal: String,
    tagCurrency: String
): TollHistoryItemEntity {
    return TollHistoryItemEntity(
        rowId = "P|${item.id}|$filterCountry",
        rowType = TollHistoryItemEntity.ROW_TYPE_PASSAGE,
        sortIndex = sortIndex,
        filterCountry = filterCountry,
        id = item.id ?: 0,
        tagsSerialNumber = serial,
        tollPlaza = item.tollPlaza ?: "",
        isPaid = item.isPaid ?: false,
        checkInDate = item.checkInDate.toEpochMillis(),
        checkOutDate = item.checkOutDate.toEpochMillis(),
        amountWithOutDiscount = item.amountWithOutDiscount ?: "",
        currency = item.currency ?: "",
        billFinal = item.bill?.billFinal ?: "",
        complaintId = item.complaint?.id,
        objectionCount = item.complaint?.objections?.filterNotNull()?.size ?: 0,
        tagTotal = tagTotal,
        tagCurrency = tagCurrency
    )
}

/** Fallback kad API ne pošalje sumTags: redosled kao u [items], promena taga otvara novu grupu. */
private fun buildRowsByApiItemOrder(
    filterCountry: String,
    startSortIndex: Int,
    page: Int,
    ordered: List<Item>,
    sumTagMap: Map<String, SumTag>
): List<TollHistoryItemEntity> {
    val result = mutableListOf<TollHistoryItemEntity>()
    var sortIndex = startSortIndex
    var currentSerial: String? = null
    var segmentIndex = 0

    fun headerRowId(serial: String) = "H|$serial|$filterCountry|p$page|s$segmentIndex"
    fun groupEndRowId(serial: String) = "G|$serial|$filterCountry|p$page|s$segmentIndex"

    for (item in ordered) {
        val serial = item.tagsSerialNumberSame ?: ""
        val matchingSumTag = sumTagMap[serial]
        val tagTotal = matchingSumTag?.total ?: ""
        val tagCurrency = matchingSumTag?.currency ?: ""

        if (serial != currentSerial) {
            if (currentSerial != null) {
                val closedTag = sumTagMap[currentSerial]
                result.add(
                    TollHistoryItemEntity(
                        rowId = groupEndRowId(currentSerial),
                        rowType = TollHistoryItemEntity.ROW_TYPE_GROUP_END,
                        sortIndex = sortIndex++,
                        filterCountry = filterCountry,
                        id = 0,
                        tagsSerialNumber = currentSerial,
                        tollPlaza = "",
                        isPaid = false,
                        checkInDate = null,
                        checkOutDate = null,
                        amountWithOutDiscount = "",
                        currency = "",
                        billFinal = "",
                        objectionCount = 0,
                        complaintId = null,
                        tagTotal = closedTag?.total ?: "",
                        tagCurrency = closedTag?.currency ?: ""
                    )
                )
            }
            currentSerial = serial
            segmentIndex++
            result.add(
                TollHistoryItemEntity(
                    rowId = headerRowId(serial),
                    rowType = TollHistoryItemEntity.ROW_TYPE_HEADER,
                    sortIndex = sortIndex++,
                    filterCountry = filterCountry,
                    id = 0,
                    tagsSerialNumber = serial,
                    tollPlaza = "",
                    isPaid = false,
                    checkInDate = null,
                    checkOutDate = null,
                    amountWithOutDiscount = "",
                    currency = "",
                    billFinal = "",
                    objectionCount = 0,
                    complaintId = null,
                    tagTotal = tagTotal,
                    tagCurrency = tagCurrency
                )
            )
        }

        result.add(
            passageEntity(
                item = item,
                serial = serial,
                filterCountry = filterCountry,
                sortIndex = sortIndex++,
                tagTotal = tagTotal,
                tagCurrency = tagCurrency
            )
        )
    }

    if (currentSerial != null) {
        val matchingSumTag = sumTagMap[currentSerial]
        result.add(
            TollHistoryItemEntity(
                rowId = "G|$currentSerial|$filterCountry|p$page|s$segmentIndex",
                rowType = TollHistoryItemEntity.ROW_TYPE_GROUP_END,
                sortIndex = sortIndex,
                filterCountry = filterCountry,
                id = 0,
                tagsSerialNumber = currentSerial,
                tollPlaza = "",
                isPaid = false,
                checkInDate = null,
                checkOutDate = null,
                amountWithOutDiscount = "",
                currency = "",
                billFinal = "",
                objectionCount = 0,
                complaintId = null,
                tagTotal = matchingSumTag?.total ?: "",
                tagCurrency = matchingSumTag?.currency ?: ""
            )
        )
    }

    return result
}

fun TollHistoryItemEntity.toUi(): TollHistoryItemUi {
    return TollHistoryItemUi(
        id = id,
        tagsSerialNumber = tagsSerialNumber,
        billFinal = billFinal,
        amountDisplay = amountWithOutDiscount,
        currencyDisplay = currency,
        tollPlaza = tollPlaza,
        checkInFormatted = checkInDate.toDisplayDate(),
        checkOutFormatted = checkOutDate.toDisplayDate(),
        isPaid = isPaid,
        complaintId = complaintId,
        objectionCount = objectionCount,
        maxObjectionsReached = objectionCount >= 2,
        tagTotal = tagTotal,
        tagCurrency = tagCurrency
    )
}


































