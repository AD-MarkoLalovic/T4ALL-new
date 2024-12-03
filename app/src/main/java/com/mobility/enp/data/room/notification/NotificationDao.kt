package com.mobility.enp.data.room.notification

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mobility.enp.data.model.notification.NotificationModel

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notificationstable")
    fun fetchNotifications(): LiveData<List<NotificationModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationModel)

    @Query("DELETE FROM notificationstable")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteOne(notification: NotificationModel)
}