package com.mobility.enp.data.room

import androidx.annotation.Keep
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mobility.enp.data.model.api_room_models.UserLoginResponseRoomTable

@Keep
@Dao
interface LoginDao {

    @Query("SELECT * FROM loginTable")
    suspend fun fetchAllowedUsers(): UserLoginResponseRoomTable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userLoginResponseRoomTable: UserLoginResponseRoomTable)

    @Query("DELETE FROM loginTable")
    suspend fun deleteAll()

}