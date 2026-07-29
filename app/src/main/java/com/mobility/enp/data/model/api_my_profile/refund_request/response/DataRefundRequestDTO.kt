package com.mobility.enp.data.model.api_my_profile.refund_request.response

import com.mobility.enp.data.model.api_my_profile.refund_request.entity.DataRefundRequestEntity

data class DataRefundRequestDTO(
    val id: Int?,
    val serialNumber: String,
    val customerId: Int,
    val account: String,
    val bank: String,
    val amount: String,
    val currency: CurrencyDTO,
    val filePath: String?,
    val status: StatusDTO,
    val registrationPlate: String?
) {
    fun toEntity(): DataRefundRequestEntity {
        return DataRefundRequestEntity(
            id = id ?: 0,
            serialNumber = serialNumber,
            customerId = customerId,
            account = account,
            bank = bank,
            amount = amount,
            currencyValue = currency.value,
            currencyText = currency.text,
            statusValue = status.value,
            statusText = status.text,
            registrationPlate = registrationPlate
        )
    }
}
