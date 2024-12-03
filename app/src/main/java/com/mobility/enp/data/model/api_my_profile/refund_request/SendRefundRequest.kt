package com.mobility.enp.data.model.api_my_profile.refund_request

data class SendRefundRequest(
    val tagSerialNumber: String,
    val accountZr: String,
    val accountBank: String,
    val amount: Int
)