package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.api_my_invoices.MyInvoicesResponse

@Dao
interface MyInvoicesDao {

    @Upsert
    suspend fun insertMonthlyInvoices(monthlyInvoices: MyInvoicesResponse)

    @Query("DELETE FROM monthly_bills")
    suspend fun deleteDataMonthlyInvoices()

    @Query("SELECT * FROM monthly_bills")
    suspend fun fetchDataMonthlyInvoices(): MyInvoicesResponse
}