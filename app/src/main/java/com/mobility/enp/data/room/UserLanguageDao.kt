package com.mobility.enp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mobility.enp.data.model.api_room_models.UserLanguage

@Dao
interface UserLanguageDao {

    @Query("SELECT * FROM languageTable")
    suspend fun fetchAllowedUsers(): UserLanguage?

    @Query("SELECT COUNT(*) FROM languageTable")
    suspend fun getTableSize(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userLanguage: UserLanguage)

    @Query("DELETE FROM languageTable")
    suspend fun deleteAll()

    @Insert
    suspend fun insertLanguage(language: UserLanguage)

    @Query("UPDATE languageTable SET userLanguage = :languageString WHERE id = :id")
    suspend fun update(id: Int, languageString: String)

}