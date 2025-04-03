package com.mobility.enp.data.model.api_my_profile.refund_request

data class SendRefundRequest(
    val tagSerialNumber: String,
    val accountBank: String,
    val accountZr: String,
    val accountZr2: String,
    val accountZr3: String,
    val amount: Int
)