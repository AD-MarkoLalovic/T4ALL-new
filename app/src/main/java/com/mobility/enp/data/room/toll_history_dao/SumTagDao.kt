package com.mobility.enp.data.room.toll_history_dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.new_toll_history.entity.SumTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SumTagDao {

    @Upsert
    suspend fun upsert(items: List<SumTagEntity>)

    @Query("SELECT * FROM new_sum_tags")
    fun observeAll(): Flow<List<SumTagEntity>>

    @Query("DELETE FROM new_sum_tags")
    suspend fun clear()

}