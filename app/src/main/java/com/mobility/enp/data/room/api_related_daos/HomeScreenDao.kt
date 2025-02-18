package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mobility.enp.data.model.home.entity.HomeEntity
import com.mobility.enp.data.model.home.entity.InvoiceHomeEntity
import com.mobility.enp.data.model.home.entity.InvoiceHomeTotalCurrencyEntity
import com.mobility.enp.data.model.home.entity.TollHistoryHomeEntity
import com.mobility.enp.data.model.home.relation.HomeWithDetails

@Dao
interface HomeScreenDao {

    @Query("DELETE FROM home_entity")
    suspend fun deleteHomeScreenData()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHome(home: HomeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTollHistory(history: List<TollHistoryHomeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoices(invoices: List<InvoiceHomeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceCurrencies(currencies: List<InvoiceHomeTotalCurrencyEntity>)

    @Transaction
    @Query("SELECT * FROM home_entity WHERE id = 1")
    suspend fun getHomeWithDetails(): HomeWithDetails?
}