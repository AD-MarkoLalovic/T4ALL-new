package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.api_tool_history.listing.ToolHistoryListing

@Dao
interface HistoryListingDao {

    @Upsert
    suspend fun insertData(toolHistoryIndex: ToolHistoryListing)

    @Query("DELETE FROM historyListing")
    suspend fun deleteData()

    @Query("SELECT * FROM historyListing")
    fun fetchData(): List<ToolHistoryListing>

    @Query("SELECT * FROM HISTORYLISTING WHERE serial = :serial")
    fun fetchPassageBySerial(serial: String): ToolHistoryListing

}