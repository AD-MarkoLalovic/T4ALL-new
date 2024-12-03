package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobility.enp.data.model.api_home_page.homedata.Promotion

@Dao
interface PromotionsDao {

    @Upsert
    suspend fun upsertPromotion(promotions: List<Promotion>)

    @Upsert
    fun upsertSinglePromotion(promotions: Promotion)

    @Upsert
    suspend fun deletedByUser(promotions: Promotion)  // modifies boolean parameter of existing object

    @Query("SELECT * FROM promotions_table")
    suspend fun getPromotionsList(): List<Promotion>

    @Query("DELETE FROM promotions_table")
    suspend fun deleteAllPromotions()

}