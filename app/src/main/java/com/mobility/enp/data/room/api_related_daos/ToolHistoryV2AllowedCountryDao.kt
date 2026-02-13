package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2AllowedCountries
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolHistoryV2AllowedCountryDao {
    @Upsert()
    suspend fun upsert(items: List<V2AllowedCountries>)

    @Query("SELECT * FROM allowed_countries")
    fun observeAllowedCountries(): Flow<List<V2AllowedCountries>>

    @Query("SELECT * FROM allowed_countries ORDER BY country DESC")
    fun observeAllStrings(): Flow<List<V2AllowedCountries>>

    @Query("DELETE FROM allowed_countries")
    suspend fun clear()
}