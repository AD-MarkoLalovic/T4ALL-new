package com.mobility.enp.data.model.home.response

import com.mobility.enp.data.model.home.entity.HomeEntity
import com.mobility.enp.data.model.home.entity.InvoiceHomeEntity
import com.mobility.enp.data.model.home.entity.InvoiceHomeTotalCurrencyEntity
import com.mobility.enp.data.model.home.entity.TollHistoryHomeEntity

data class Data(
    val customer: Customer,
    val tollHistory: List<TollHistory?>?,
    val invoices: List<Invoice?>?,
    val promotions: List<Any?>?

) {
    fun toHomeEntity(): HomeEntity {
        return HomeEntity(
            firstName = customer.firstName,
            lastName = customer.lastName,
            displayName = customer.displayName,
            customerType = customer.customerType.type,
            portalKey = customer.portalKey
        )
    }

    // Mapiranje na listu TollHistoryHomeEntity
    fun toHomeTollHistory(homeId: Int): List<TollHistoryHomeEntity> {
        return tollHistory?.mapNotNull { it ->
            it?.let {
                TollHistoryHomeEntity(
                    homeId = homeId,
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


    // Mapiranje na listu InvoiceHomeEntity
    fun toHomeInvoices(homeId: Int): List<InvoiceHomeEntity> {
        return invoices?.map {
            InvoiceHomeEntity(
                homeId = homeId,
                monthName = it?.month?.name ?: "",
                year = it?.year ?: ""
            )
        } ?: emptyList()
    }

    // Mapiranje na listu InvoiceHomeTotalCurrencyEntity
    fun toHomeInvoiceCurrencies(invoiceId: Int): List<InvoiceHomeTotalCurrencyEntity> {
        return invoices?.flatMap {
            it?.totalCurrency?.map { totalCurrency ->
                InvoiceHomeTotalCurrencyEntity(
                    invoiceId = invoiceId,
                    total = totalCurrency.total,
                    isPaid = totalCurrency.isPaid,
                    currencyValue = totalCurrency.currency.value
                )
            } ?: emptyList()
        } ?: emptyList()
    }
}