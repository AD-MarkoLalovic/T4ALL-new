package com.mobility.enp.data.model.home.response

import com.mobility.enp.data.model.home.entity.CustomerHomeEntity
import com.mobility.enp.data.model.home.entity.InvoiceHomeEntity
import com.mobility.enp.data.model.home.entity.TollHistoryHomeEntity

data class Data(
    val customer: Customer?,
    val tollHistory: List<TollHistory?>?,
    val invoices: List<Invoice?>?,
    val promotions: List<Any?>?

) {
    fun toCustomerEntity(): CustomerHomeEntity? {
        return customer?.let {
            CustomerHomeEntity(
                firstName = it.firstName,
                lastName = it.lastName,
                displayName = it.displayName,
                customerType = it.customerType.type
            )
        }
    }

    fun toTollHistoryEntities(): List<TollHistoryHomeEntity> {
        return tollHistory?.mapNotNull { toll ->
            toll?.let {
                TollHistoryHomeEntity(
                    invoiceNumber = it.invoiceNumber,
                    status = it.status?.value,
                    entryToll = it.entryToll,
                    exitToll = it.exitToll,
                    entryDate = it.entryDate,
                    exitDate = it.exitDate,
                    entryTime = it.entryTime,
                    exitTime = it.exitTime,
                    paymentAmount = it.paymentAmount?.amount,
                    paymentCurrency = it.paymentAmount?.currency
                )
            }
        } ?: emptyList()
    }

    fun toInvoiceEntities(): List<InvoiceHomeEntity> {
        return invoices?.mapNotNull { invoice ->
            invoice?.let {
                InvoiceHomeEntity(
                    month = it.month.name,
                    year = it.year,
                    total = it.totalCurrency.firstOrNull()?.total ?: "0,00",
                    isPaid = it.totalCurrency.firstOrNull()?.isPaid ?: false,
                    currency = it.totalCurrency.firstOrNull()?.currency?.label ?: "RSD"
                )
            }
        } ?: emptyList()
    }
}