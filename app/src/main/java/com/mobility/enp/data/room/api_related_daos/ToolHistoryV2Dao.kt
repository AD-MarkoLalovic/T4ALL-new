package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponse

@Dao
interface ToolHistoryV2Dao {

    @Upsert
    suspend fun insertData(data: V2HistoryTagResponse)

    @Query("DELETE FROM HISTORY_V2")
    suspend fun deleteData()

    @Query("SELECT * FROM HISTORY_V2")
    fun fetchData(): List<V2HistoryTagResponse>

    @Query("SELECT * FROM HISTORY_V2 WHERE serial = :serial")
    fun fetchPassageBySerial(serial: String): V2HistoryTagResponse

}