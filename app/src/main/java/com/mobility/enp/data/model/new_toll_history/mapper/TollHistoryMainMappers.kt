package com.mobility.enp.data.model.new_toll_history.mapper

import  android.content.Context
import com.mobility.enp.R
import com.mobility.enp.data.model.cardsweb.Data
import com.mobility.enp.data.model.new_toll_history.local.entity.AllowedCountryEntity
import com.mobility.enp.data.model.new_toll_history.local.entity.TollHistoryItemEntity
import com.mobility.enp.data.model.new_toll_history.remote.dto.Item
import com.mobility.enp.data.model.new_toll_history.remote.dto.SumTag
import com.mobility.enp.data.model.new_toll_history.remote.dto.TollHistoryDto
import com.mobility.enp.view.ui_models.toll_history.AllowedCountryUi
import com.mobility.enp.view.ui_models.toll_history.TollHistoryItemUi
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.Instant

private val apiDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

private val displayDataFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss", Locale.ENGLISH)

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

fun Data.countriesToEntity(): List<AllowedCountryEntity> {
    return buildList {
        if (showTabRS) add(AllowedCountryEntity("RS", size))
        if (showTabME) add(AllowedCountryEntity("ME", size))
        if (showTabMK) add(AllowedCountryEntity("MK", size))
        if (showTabHR) add(AllowedCountryEntity("HR", size))
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

fun TollHistoryDto.toTollHistoryEntities(
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
            createHeaderEntity(
                serial = serial,
                filterCountry = filterCountry,
                sortIndex = sortIndex++,
                tagTotal = tagTotal,
                tagCurrency = tagCurrency,
                page = page,
                segmentIndex = segmentIndex
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
            createGroupEndEntity(
                serial = serial,
                filterCountry = filterCountry,
                sortIndex = sortIndex++,
                tagTotal = tagTotal,
                tagCurrency = tagCurrency,
                page = page,
                segmentIndex = segmentIndex
            )
        )
    }
    return result
}

private fun createHeaderEntity(
    serial: String,
    filterCountry: String,
    sortIndex: Int,
    tagTotal: String,
    tagCurrency: String,
    page: Int,
    segmentIndex: Int
): TollHistoryItemEntity = TollHistoryItemEntity(
    rowId = "H|$serial|$filterCountry|p$page|s$segmentIndex",
    rowType = TollHistoryItemEntity.ROW_TYPE_HEADER,
    sortIndex = sortIndex,
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
    tagCurrency = tagCurrency,
    entryTime = null,
    exitTime = null,
    ticketUid = null
)

private fun createGroupEndEntity(
    serial: String,
    filterCountry: String,
    sortIndex: Int,
    tagTotal: String,
    tagCurrency: String,
    page: Int,
    segmentIndex: Int
): TollHistoryItemEntity = TollHistoryItemEntity(
    rowId = "G|$serial|$filterCountry|p$page|s$segmentIndex",
    rowType = TollHistoryItemEntity.ROW_TYPE_GROUP_END,
    sortIndex = sortIndex,
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
    tagCurrency = tagCurrency,
    entryTime = null,
    exitTime = null,
    ticketUid = null
)

private fun passageEntity(
    item: Item,
    serial: String,
    filterCountry: String,
    sortIndex: Int,
    tagTotal: String,
    tagCurrency: String
): TollHistoryItemEntity {
    val rowSuffix = when {
        item.id != null -> item.id.toString()
        !item.ticketUid.isNullOrBlank() -> item.ticketUid
        else -> 0
    }
    return TollHistoryItemEntity(
        rowId = "P|$rowSuffix|$filterCountry",
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
        tagCurrency = tagCurrency,
        entryTime = item.entryTime ?: "",
        exitTime = item.exitTime ?: "",
        ticketUid = item.ticketUid
    )
}

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

    for (item in ordered) {
        val serial = item.serialNumber ?: item.tagsSerialNumber ?: ""
        val matchingSumTag = sumTagMap[serial]
        val tagTotal = matchingSumTag?.total ?: ""
        val tagCurrency = matchingSumTag?.currency ?: ""

        if (serial != currentSerial) {
            if (currentSerial != null) {
                val closedTag = sumTagMap[currentSerial]
                result.add(
                    createGroupEndEntity(
                        serial = currentSerial,
                        filterCountry = filterCountry,
                        sortIndex = sortIndex++,
                        tagTotal = closedTag?.total ?: "",
                        tagCurrency = closedTag?.currency ?: "",
                        page = page,
                        segmentIndex = segmentIndex
                    )
                )
            }
            currentSerial = serial
            segmentIndex++
            result.add(
                createHeaderEntity(
                    serial = currentSerial,
                    filterCountry = filterCountry,
                    sortIndex = sortIndex++,
                    tagTotal = tagTotal,
                    tagCurrency = tagCurrency,
                    page = page,
                    segmentIndex = segmentIndex
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
            createGroupEndEntity(
                serial = currentSerial,
                filterCountry = filterCountry,
                sortIndex = sortIndex,
                tagTotal = matchingSumTag?.total ?: "",
                tagCurrency = matchingSumTag?.currency ?: "",
                page = page,
                segmentIndex = segmentIndex
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
        tagCurrency = tagCurrency,
        entryTime = entryTime,
        exitTime = exitTime,
        ticketUid = ticketUid
    )
}