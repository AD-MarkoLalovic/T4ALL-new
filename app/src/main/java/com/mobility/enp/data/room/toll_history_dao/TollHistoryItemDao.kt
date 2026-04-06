package com.mobility.enp.data.room.toll_history_dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.new_toll_history.local.entity.TollHistoryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TollHistoryItemDao {

    @Upsert
    suspend fun upsertAll(items: List<TollHistoryItemEntity>)

    @Query("DELETE FROM new_toll_history_items")
    suspend fun clear()

    @Query("""
        SELECT * FROM new_toll_history_items 
        WHERE filterCountry = :country
        ORDER BY tagsSerialNumber ASC,  checkOutDate DESC
    """)
    fun pagingSource(country: String): PagingSource<Int, TollHistoryItemEntity>

    @Query("DELETE FROM new_toll_history_items WHERE filterCountry = :country")
    suspend fun deleteByQuery(country: String)





    @Query("""
        SELECT * FROM new_toll_history_items 
        WHERE tagsSerialNumber = :serial 
        AND filterCountry = :country
        ORDER BY checkOutDate DESC
    """)
    fun observeBySerialAndCountry(
        serial: String,
        country: String
    ): Flow<List<TollHistoryItemEntity>>


    @Query("""
        SELECT * FROM new_toll_history_items 
        WHERE tagsSerialNumber = :serial
        ORDER BY checkOutDate DESC
    """)
    fun observeBySerial(serial: String): Flow<List<TollHistoryItemEntity>>



}