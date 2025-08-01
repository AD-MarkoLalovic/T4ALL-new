package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface HistoryIndexDao {

    @Upsert
    suspend fun insertData(toolHistoryIndex: IndexData)

    @Query("DELETE FROM indexdata")
    suspend fun deleteData()

    @Query("SELECT * FROM indexdata")
    fun fetchData(): IndexData

}