package com.mobility.enp.data.room.toll_history_dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.new_toll_history.entity.AllowedCountryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AllowedCountryDao {

    @Upsert
    suspend fun upsert(item: List<AllowedCountryEntity>)

    @Query("SELECT * FROM new_allowed_countries ORDER BY position ASC")
    fun observeAll(): Flow<List<AllowedCountryEntity>>

    @Query("DELETE FROM new_allowed_countries")
    suspend fun clear()
}