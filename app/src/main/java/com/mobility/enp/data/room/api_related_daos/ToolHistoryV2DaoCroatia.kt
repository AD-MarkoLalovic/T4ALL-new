package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponseCroatia
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolHistoryV2DaoCroatia {

    @Upsert
    suspend fun upsertData(data: V2HistoryTagResponseCroatia)

    @Query("DELETE FROM HISTORY_V2_Croatia")
    suspend fun deleteData()

    @Query("SELECT * FROM HISTORY_V2_Croatia")
    fun fetchData(): List<V2HistoryTagResponseCroatia>

    @Query("SELECT * FROM HISTORY_V2_Croatia WHERE serial = :serial AND countryCode = :countryCode")
    fun fetchPassageBySerial(serial: String, countryCode: String): V2HistoryTagResponseCroatia?

    @Query("SELECT * FROM HISTORY_V2_Croatia WHERE serial = :serial AND pageNumber = :page")
    fun observePassageData(serial: String, page: Int): Flow<List<V2HistoryTagResponseCroatia?>>

}
