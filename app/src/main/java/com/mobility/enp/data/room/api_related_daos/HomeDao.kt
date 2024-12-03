package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.api_home_page.homedata.HomeScreenData

@Dao
interface HomeDao {

    @Upsert
    suspend fun insertData(homeScreenData: HomeScreenData)

    @Query("DELETE FROM homedata")
    suspend fun deleteData()

    @Query("SELECT * FROM homedata")
    suspend fun fetchData(): HomeScreenData

}