package com.mobility.enp.view.ui_models.toll_history

sealed interface TollHistoryFilterExportState {

    data object Idle : TollHistoryFilterExportState

    data object Loading : TollHistoryFilterExportState

    data class CsvReady(
        val base64Content: String,
        val fileNameSuffix: String
    ) : TollHistoryFilterExportState

    data class PdfReady(
        val filePath: String
    ) : TollHistoryFilterExportState

    data class Error(
        val message: String,
        val isNoConnection: Boolean = false,
        val isInvalidToken: Boolean = false
    ) : TollHistoryFilterExportState
}
