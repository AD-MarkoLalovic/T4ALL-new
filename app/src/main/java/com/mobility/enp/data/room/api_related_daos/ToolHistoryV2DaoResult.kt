package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponseResult
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolHistoryV2DaoResult {

    @Upsert
    suspend fun upsert(data: V2HistoryTagResponseResult)

    @Upsert
    suspend fun upsertAll(data: List<V2HistoryTagResponseResult>)

    @Query("DELETE FROM HISTORY_V2_Result")
    suspend fun deleteData()

    @Query("SELECT * FROM HISTORY_V2_Result WHERE serial = :serial AND countryCode = :countryCode")
    fun observePassageDataBySerialAndCountryCodeLoad(
        serial: String,
        countryCode: String
    ): List<V2HistoryTagResponseResult?>

    @Query("SELECT * FROM HISTORY_V2_Result WHERE serial = :serial AND countryCode = :countryCode")
    fun observePassageDataBySerialAndCountryCode(
        serial: String,
        countryCode: String
    ): Flow<List<V2HistoryTagResponseResult?>>

    @Query("SELECT * FROM HISTORY_V2_Result WHERE serial = :serial AND currentPage = :page")
    fun observePassageData(serial: String, page: Int): Flow<List<V2HistoryTagResponseResult?>>

    @Query("SELECT * FROM HISTORY_V2_Result WHERE serial = :serial")
    fun observePassageDataBySerial(serial: String): Flow<List<V2HistoryTagResponseResult?>>
}