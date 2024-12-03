package com.mobility.enp.data.model.api_my_profile.refund_request.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mobility.enp.view.ui_models.refund_request.RefundRequestUIModel

@Entity(tableName = "refund_request")
data class DataRefundRequestEntity(
    @PrimaryKey
    val id: Int,
    val serialNumber: String,
    val customerId: Int,
    val account: String,
    val bank: String,
    val amount: String,
    val currencyValue: String,
    val currencyText: String,
    val statusValue: Int,
    val statusText: String,
    val registrationPlate: String
) {
    fun toUIModel(): RefundRequestUIModel {
        return RefundRequestUIModel(
            id = id,
            serialNumber = serialNumber,
            displayAmount = "$amount $currencyText",
            registrationPlate = registrationPlate,
            statusText = statusText,
            statusValue = statusValue,
            bankAccount = account,
            bank = bank
        )
    }


}

