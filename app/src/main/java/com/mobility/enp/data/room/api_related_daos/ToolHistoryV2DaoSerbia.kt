package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponse
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponseSerbia

@Dao
interface ToolHistoryV2DaoSerbia {

    @Upsert
    suspend fun insertData(data: V2HistoryTagResponseSerbia)

    @Query("DELETE FROM HISTORY_V2_Serbia")
    suspend fun deleteData()

    @Query("SELECT * FROM HISTORY_V2_Serbia")
    fun fetchData(): List<V2HistoryTagResponseSerbia>

    @Query("SELECT * FROM HISTORY_V2_Serbia WHERE serial = :serial AND countryCode = :countryCode")
    fun fetchPassageBySerial(serial: String,countryCode: String): V2HistoryTagResponseSerbia?

}