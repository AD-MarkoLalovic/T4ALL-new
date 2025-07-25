package com.mobility.enp.data.repository

import android.content.Context
import com.mobility.enp.data.model.api_my_invoices.refactor.MyInvoicesResponse
import com.mobility.enp.data.model.pdf_table.PdfTable
import com.mobility.enp.data.room.database.DRoom

class BillsRepository(dRoom: DRoom, context: Context) : BaseRepository(dRoom, context) {


    suspend fun getTokenTemp(): String? {
        val userToken = getUserToken()
        return userToken
    }

    fun isNetworkPresent(): Boolean {
        return isNetworkAvailable()
    }

    suspend fun setLocalBillsData(bills: MyInvoicesResponse) {
        database.myInvoicesDao().deleteDataMonthlyInvoices()
        database.myInvoicesDao().insertMonthlyInvoices(bills)
    }

    suspend fun fetchSavedBillsData(): MyInvoicesResponse {
        return database.myInvoicesDao().fetchDataMonthlyInvoices()
    }

    suspend fun savePdfData(decodedData: ByteArray) {
        database.pdfDao().deleteData()
        database.pdfDao().upsertData(PdfTable(0, decodedData))
    }

    suspend fun getPdfTable(): PdfTable{
        return database.pdfDao().fetchData()
    }

}