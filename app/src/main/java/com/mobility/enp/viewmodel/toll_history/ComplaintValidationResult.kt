package com.mobility.enp.viewmodel.toll_history

sealed class ComplaintValidationResult {
    object Valid : ComplaintValidationResult()
    object EmptyRequiredFields : ComplaintValidationResult()
    object ReasonTooShort : ComplaintValidationResult()
    object NoBankSelected : ComplaintValidationResult()
    object MissingBankFields : ComplaintValidationResult()
    object InvalidAccountNumber : ComplaintValidationResult()
}