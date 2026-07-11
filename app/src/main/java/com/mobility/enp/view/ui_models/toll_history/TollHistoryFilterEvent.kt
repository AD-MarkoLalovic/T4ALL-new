package com.mobility.enp.view.ui_models.toll_history

sealed interface TollHistoryFilterEvent {

    data class NavigateToResults(
        val filter: TollHistoryFilterUi
    ) : TollHistoryFilterEvent

    data class ShowToast(
        val messageRes: Int
    ) : TollHistoryFilterEvent

    data class ShowNoInternetDialog(
        val titleRes: Int,
        val subtitleRes: Int
    ) : TollHistoryFilterEvent

    data object ShowExportMenu : TollHistoryFilterEvent

    data class RequestNotificationPermission(
        val exportType: ExportType
    ) : TollHistoryFilterEvent

    data object LogoutOnInvalidToken : TollHistoryFilterEvent
}
