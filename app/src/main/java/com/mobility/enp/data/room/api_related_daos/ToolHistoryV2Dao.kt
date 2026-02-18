package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponse
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolHistoryV2Dao {

    @Upsert
    suspend fun upsert(data: V2HistoryTagResponse)

    @Query("DELETE FROM HISTORY_V2")
    suspend fun deleteData()

    @Query("SELECT * FROM HISTORY_V2 WHERE serial = :serial AND countryCode = :countryCode")
    fun observePassageDataBySerialAndCountryCodeLoad(
        serial: String,
        countryCode: String
    ): List<V2HistoryTagResponse?>

    @Query("SELECT * FROM HISTORY_V2 WHERE serial = :serial AND countryCode = :countryCode")
    fun observePassageDataBySerialAndCountryCode(
        serial: String,
        countryCode: String
    ): Flow<List<V2HistoryTagResponse?>>

    @Query("SELECT * FROM HISTORY_V2 WHERE serial = :serial AND currentPage = :page")
    fun observePassageData(serial: String, page: Int): Flow<List<V2HistoryTagResponse?>>

    @Query("SELECT * FROM HISTORY_V2 WHERE serial = :serial")
    fun observePassageDataBySerial(serial: String): Flow<List<V2HistoryTagResponse?>>
}