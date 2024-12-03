package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mobility.enp.data.model.IntroPageStatus

@Dao
interface IntroPageStatusDao {

    @Query("SELECT * FROM intro_page_status WHERE `key` = :key")
    fun getByKey(key: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveIntroPageShown(introPageStatus: IntroPageStatus)

    @Query("DELETE FROM intro_page_status")
    fun deleteAll()
}