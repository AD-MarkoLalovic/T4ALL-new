package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponseNorthMacedonia
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponseSerbia
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolHistoryV2DaoNorthMacedonia {

    @Upsert
    suspend fun upsertData(data: V2HistoryTagResponseNorthMacedonia)

    @Query("DELETE FROM HISTORY_V2_NorthMacedonia")
    suspend fun deleteData()

    @Query("SELECT * FROM HISTORY_V2_NorthMacedonia")
    fun fetchData(): List<V2HistoryTagResponseNorthMacedonia>

    @Query("SELECT * FROM HISTORY_V2_NorthMacedonia WHERE serial = :serial AND countryCode = :countryCode")
    fun fetchPassageBySerial(
        serial: String,
        countryCode: String
    ): V2HistoryTagResponseNorthMacedonia?

    @Query("SELECT * FROM HISTORY_V2_Serbia WHERE serial = :serial AND countryCode = :page")
    fun observePassageData(serial: String, page: Int): Flow<List<V2HistoryTagResponseNorthMacedonia>>?

}
