package com.mobility.enp.data.room.api_related_daos

import androidx.annotation.Keep
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.mobility.enp.data.model.api_room_models.FcmToken

@Keep
@Dao
interface FcmTokenDao {
    @Insert
    suspend fun insertData(fcmToken: FcmToken)

    @Query("DELETE FROM fcm_token")
    suspend fun deleteTable()

    @Query("SELECT * FROM fcm_token")
    suspend fun getTableData(): FcmToken
}