package com.mobility.enp.viewmodel.toll_history

sealed class ObjectionValidationResult {
    object Valid : ObjectionValidationResult()
    object EmptyRequiredFields : ObjectionValidationResult()
    object ReasonTooShort : ObjectionValidationResult()
}