package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponse
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponseMontenegro

@Dao
interface ToolHistoryV2DaoMontenegro {

    @Upsert
    suspend fun insertData(data: V2HistoryTagResponseMontenegro)

    @Query("DELETE FROM HISTORY_V2_Montenegro")
    suspend fun deleteData()

    @Query("SELECT * FROM HISTORY_V2_Montenegro")
    fun fetchData(): List<V2HistoryTagResponseMontenegro>

    @Query("SELECT * FROM HISTORY_V2_Montenegro WHERE serial = :serial AND countryCode = :countryCode")
    fun fetchPassageBySerial(serial: String,countryCode: String): V2HistoryTagResponseMontenegro?

}