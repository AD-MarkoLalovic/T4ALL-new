package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolHistoryV2TagsSerials {

    @Upsert
    suspend fun upsertData(toolHistoryIndex: IndexData)

    @Upsert
    suspend fun upsertDataAll(toolHistoryIndex: List<IndexData>)

    @Query("DELETE FROM HISTORY_V2_TAGS")
    suspend fun deleteData()

    @Query("SELECT * FROM HISTORY_V2_TAGS")
    fun fetchData(): IndexData?

    @Query("SELECT * FROM HISTORY_V2_TAGS")
    fun observeIndexData(): Flow<List<IndexData>>


}