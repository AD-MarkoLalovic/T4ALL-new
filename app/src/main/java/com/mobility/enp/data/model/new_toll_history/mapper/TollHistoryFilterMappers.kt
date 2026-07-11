package com.mobility.enp.data.model.new_toll_history.mapper

import com.mobility.enp.data.model.new_toll_history.tags.remote.Tag
import com.mobility.enp.data.model.new_toll_history.tags.remote.TollHistoryTagsDto
import com.mobility.enp.view.ui_models.toll_history.TollHistoryFilterDateUi
import com.mobility.enp.view.ui_models.toll_history.TollHistoryFilterTagUi
import com.mobility.enp.view.ui_models.toll_history.TollHistoryTagsLoadState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val filterDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)

fun Long.toFilterDateUi(): TollHistoryFilterDateUi {
    val localDate = Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    return localDate.toFilterDateUi()
}

fun Tag.toFilterTagUi(
    isSelected: Boolean,
    isLastItem: Boolean = false
): TollHistoryFilterTagUi {
    val plate = registrationPlate?.takeIf { it.isNotEmpty() }
    return TollHistoryFilterTagUi(
        serialNumber = serialNumber,
        registrationPlate = plate ?: serialNumber,
        showSerialNumber = plate != null,
        isSelected = isSelected,
        isLastItem = isLastItem,
        categoryText = category?.text,
        categoryValue = category?.value
    )
}

fun List<Tag>.toFilterTagUiList(
    selectedSerials: Set<String>,
    allSelected: Boolean
): List<TollHistoryFilterTagUi> {
    if (isEmpty()) return emptyList()
    return mapIndexed { index, tag ->
        val isSelected = allSelected || selectedSerials.contains(tag.serialNumber)
        tag.toFilterTagUi(
            isSelected = isSelected,
            isLastItem = index == lastIndex
        )
    }
}

fun defaultFilterDates(): Pair<TollHistoryFilterDateUi, TollHistoryFilterDateUi> {
    val now = LocalDate.now()
    val dateFrom = now.minusDays(30)
    return dateFrom.toFilterDateUi() to now.toFilterDateUi()
}

fun LocalDate.toFilterDateUi(): TollHistoryFilterDateUi {
    val apiFormat = format(filterDateFormatter)
    return TollHistoryFilterDateUi(
        displayText = apiFormat,
        apiFormat = apiFormat,
        epochMillis = atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
}

fun TollHistoryTagsDto.toTagsLoadState(
    selectedSerials: Set<String>,
    allSelected: Boolean
): TollHistoryTagsLoadState {
    val tags = data?.tags?.filterNotNull().orEmpty()
    if (tags.isEmpty()) return TollHistoryTagsLoadState.Empty
    return TollHistoryTagsLoadState.Success(
        tags = tags.toFilterTagUiList(
            selectedSerials = selectedSerials,
            allSelected = allSelected
        )
    )
}
