package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mobility.enp.data.model.ProfileImage

@Dao
interface ProfileImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(profileImage: ProfileImage)

    @Query("SELECT * FROM profile_image WHERE displayName = :displayName")
    suspend fun getProfileImage(displayName: String): ProfileImage?

    @Query("DELETE FROM profile_image WHERE displayName = :displayName")
    suspend fun deleteImage(displayName: String)

    @Query("DELETE FROM profile_image")
    suspend fun deleteAll()

    @Query("SELECT * FROM profile_image ")
    suspend fun selectAll():List<ProfileImage>
}