package com.mobility.enp.data.room.toll_history_dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.new_toll_history.local.entity.SumTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SumTagDao {

    @Upsert
    suspend fun upsert(items: List<SumTagEntity>)

    @Query("SELECT * FROM  new_sum_tags ORDER BY position ASC")
    fun observeAll(): Flow<List<SumTagEntity>>

    @Query("DELETE FROM new_sum_tags")
    suspend fun clear()

}