package com.mobility.enp.data.room.toll_history_dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.new_toll_history.entity.TollHistoryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TollHistoryItemDao {

    @Upsert
    suspend fun upsertAll(items: List<TollHistoryItemEntity>)

    @Query("""
        SELECT * FROM new_toll_history_items 
        WHERE tagsSerialNumber = :serial 
        AND countryCode = :country
        ORDER BY checkOutDate DESC
    """)
    fun observeBySerialAndCountry(
        serial: String,
        country: String
    ): Flow<List<TollHistoryItemEntity>>

    @Query("DELETE FROM new_toll_history_items")
    suspend fun clear()


    @Query("""
        SELECT * FROM new_toll_history_items 
        WHERE tagsSerialNumber = :serial
        ORDER BY checkOutDate DESC
    """)
    fun observeBySerial(serial: String): Flow<List<TollHistoryItemEntity>>

    @Query("DELETE FROM new_toll_history_items WHERE tagsSerialNumber = :serial")
    suspend fun deleteBySerial(serial: String)
}