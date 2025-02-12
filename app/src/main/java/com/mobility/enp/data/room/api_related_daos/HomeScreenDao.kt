package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.home.entity.HomeEntity

@Dao
interface HomeScreenDao {
    @Upsert
    suspend fun insertHomeData(homeScreenData: HomeEntity)

    @Query("DELETE FROM home_entity")
    suspend fun deleteHomeScreenData()

    @Query("SELECT * FROM home_entity")
    suspend fun fetchHomeData(): HomeEntity?
}