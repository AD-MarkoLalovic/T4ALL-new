package com.mobility.enp.data.room.api_related_daos

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.mobility.enp.data.model.home.entity.HomeEntity
import com.mobility.enp.data.model.home.entity.InvoiceHomeEntity
import com.mobility.enp.data.model.home.entity.InvoiceHomeTotalCurrencyEntity
import com.mobility.enp.data.model.home.entity.TollHistoryHomeEntity
import com.mobility.enp.data.model.home.relation.HomeWithDetails

@Dao
interface HomeScreenDao {

    // Brisanje svih podataka iz tabele home_entity
    @Query("DELETE FROM home_entity")
    suspend fun deleteHomeScreenData()

    // Brisanje svih podataka iz tabele toll_history_home_entity
    @Query("DELETE FROM toll_history_home")
    suspend fun deleteTollHistoryData()

    // Brisanje svih podataka iz tabele invoice_home_entity
    @Query("DELETE FROM invoices_home")
    suspend fun deleteInvoiceData()

    // Brisanje svih podataka iz tabele invoice_home_total_currency_entity
    @Query("DELETE FROM invoices_home_total_currency")
    suspend fun deleteInvoiceCurrencyData()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeManual(home: HomeEntity): Long

    @Upsert
    suspend fun insertTollHistory(tollHistory: List<TollHistoryHomeEntity>)

    @Upsert
    suspend fun insertInvoice(invoice: List<InvoiceHomeEntity>): List<Long>

    @Upsert
    suspend fun insertInvoiceCurrency(currency: List<InvoiceHomeTotalCurrencyEntity>)

    @Transaction
    @Query("SELECT * FROM home_entity WHERE id = 1")
    suspend fun getHomeWithDetails(): HomeWithDetails?
}