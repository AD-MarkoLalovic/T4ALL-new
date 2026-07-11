package com.mobility.enp.view.ui_models.toll_history

sealed interface TollHistoryTagsLoadState {

    data object Loading : TollHistoryTagsLoadState

    data object Empty : TollHistoryTagsLoadState

    data class Success(
        val tags: List<TollHistoryFilterTagUi>
    ) : TollHistoryTagsLoadState

    data class Error(
        val message: String,
        val isNoConnection: Boolean = false
    ) : TollHistoryTagsLoadState
}
