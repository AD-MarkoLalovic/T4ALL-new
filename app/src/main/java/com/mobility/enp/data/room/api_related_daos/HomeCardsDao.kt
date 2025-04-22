package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mobility.enp.data.model.home.cards.entity.HomeCardsEntity

@Dao
interface HomeCardsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeCards(cards: List<HomeCardsEntity>)

    @Update
    suspend fun updatePromotionCard(cad: HomeCardsEntity)

    @Query("SELECT * FROM home_cards WHERE email = :userEmail")
    suspend fun getHomeCardsList(userEmail: String): List<HomeCardsEntity>

}