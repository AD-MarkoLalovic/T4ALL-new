package com.mobility.enp.view.ui_models.toll_history

sealed interface TollHistoryFilterValidation {

    data object Valid : TollHistoryFilterValidation

    sealed interface Invalid : TollHistoryFilterValidation {
        data object NoTagsSelected : Invalid
        data object NoCountrySelected : Invalid
        data object NoDateRange : Invalid
        data object NoPassageData : Invalid
    }
}
