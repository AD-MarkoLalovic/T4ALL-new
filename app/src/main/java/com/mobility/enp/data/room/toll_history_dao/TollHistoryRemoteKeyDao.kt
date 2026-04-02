package com.mobility.enp.data.room.toll_history_dao

import androidx.room.Dao
import androidx.room.Upsert

@Dao
interface TollHistoryRemoteKeyDao {

    @Upsert
    suspend fun upsert(keys)
}