package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.api_my_profile.basic_information.UserInfoData
import kotlinx.coroutines.flow.Flow

@Dao
interface BasicInformationDao {

    @Upsert
    fun insertBasicInfoData(userInfo: UserInfoData)

    @Query("DELETE FROM user_information")
    fun deleteBasicInfoData()

    @Query("SELECT * FROM user_information")
    fun fetchBasicInformationData(): Flow<UserInfoData?>

}