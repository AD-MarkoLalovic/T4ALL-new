package com.mobility.enp.data.room.toll_history_dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.new_toll_history.local.entity.TollHistoryRemoteKeyEntity

@Dao
interface TollHistoryRemoteKeyDao {
    @Upsert
    suspend fun upsert(key: TollHistoryRemoteKeyEntity)

    @Query("SELECT * FROM th_remote_keys WHERE queryKey = :key")
    suspend fun getByKey(key: String): TollHistoryRemoteKeyEntity?

    @Query("DELETE FROM th_remote_keys WHERE queryKey = :key")
    suspend fun deleteByKey(key: String)

    @Query("DELETE FROM th_remote_keys")
    suspend fun clearAll()
}
