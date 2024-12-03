package com.mobility.enp.util

sealed class SubmitResultCustomerSupport {
    object Success : SubmitResultCustomerSupport()
    object Failure : SubmitResultCustomerSupport()
    object NoNetwork : SubmitResultCustomerSupport()
    object Loading : SubmitResultCustomerSupport()
}
