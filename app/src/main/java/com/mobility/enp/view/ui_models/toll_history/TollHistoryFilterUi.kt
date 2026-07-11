package com.mobility.enp.view.ui_models.toll_history

data class TollHistoryFilterUi(
    val selectedTagSerials: Set<String> = emptySet(),
    val allTagsSelected: Boolean = false,
    val countryCode: String = "",
    val dateFrom: TollHistoryFilterDateUi? = null,
    val dateTo: TollHistoryFilterDateUi? = null
) {
    val hasSelectedTags: Boolean
        get() = allTagsSelected || selectedTagSerials.isNotEmpty()

    val isCountrySelected: Boolean
        get() = countryCode.isNotEmpty()

    val isDateRangeComplete: Boolean
        get() = dateFrom != null && dateTo != null

    val isSearchValid: Boolean
        get() = hasSelectedTags && isCountrySelected && isDateRangeComplete

    val apiDateFrom: String
        get() = dateFrom?.apiFormat.orEmpty()

    val apiDateTo: String
        get() = dateTo?.apiFormat.orEmpty()
}
