package com.mobility.enp.data.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface LastUserDao {

    @Upsert
    suspend fun upsertLastUser(lastUser: LastUser)

    @Query("SELECT * FROM last_user")
    suspend fun getLastUser(): LastUser?

    @Query("DELETE FROM last_user")
    suspend fun deleteLastUser()

}