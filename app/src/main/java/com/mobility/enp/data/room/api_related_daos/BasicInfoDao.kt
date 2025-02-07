package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.api_my_profile.basic_information.entity.BasicInfoEntity

@Dao
interface BasicInfoDao {

    @Upsert
    suspend fun insertBasicInfo(data: BasicInfoEntity)

    @Query("SELECT * FROM basic_user_info")
    suspend fun getBasicInfo(): BasicInfoEntity?

    @Query("DELETE FROM basic_user_info")
    suspend fun deleteBasicInfo()
}