package com.mobility.enp.data.room.toll_history_dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.new_toll_history.local.entity.TollHistoryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TollHistoryItemDao {

    @Upsert
    suspend fun upsertAll(items: List<TollHistoryItemEntity>)

    @Query("DELETE FROM new_toll_history_items")
    suspend fun clear()

    @Query("""
        SELECT * FROM new_toll_history_items
        WHERE filterCountry = :country
        ORDER BY sortIndex ASC
    """)
    fun pagingSource(country: String): PagingSource<Int, TollHistoryItemEntity>

    @Query("DELETE FROM new_toll_history_items WHERE filterCountry = :country")
    suspend fun deleteByQuery(country: String)

    @Query(
        """
    SELECT COALESCE(MAX(sortIndex), -1)
    FROM new_toll_history_items
    WHERE filterCountry = :country
    """
    )
    suspend fun maxSortIndexForCountry(country: String): Int

}