package com.mobility.enp.view.ui_models.toll_history

data class TollHistoryFilterScreenUiState(
    val tagsLoadState: TollHistoryTagsLoadState = TollHistoryTagsLoadState.Loading,
    val countries: List<AllowedCountryUi> = emptyList(),
    val filter: TollHistoryFilterUi = TollHistoryFilterUi(),
    val isSearchEnabled: Boolean = false,
    val isExportVisible: Boolean = true,
    val showAllTagsChecked: Boolean = false
)
