package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mobility.enp.data.model.home.cards.added_cards.entity.AddedCardsEntity
import com.mobility.enp.data.model.home.cards.entity.HomeCardsEntity

@Dao
interface HomeCardsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeCards(cards: List<HomeCardsEntity>)

    @Update
    suspend fun deleteHomeCard(cad: HomeCardsEntity)

    @Query("SELECT * FROM home_cards")
    suspend fun getHomeCardsList(): List<HomeCardsEntity>?

    @Query("DELETE FROM home_cards")
    suspend fun deleteAllCards()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddedCards(cards: List<AddedCardsEntity>)

    @Query("SELECT * from added_cards")
    suspend fun getAddedCards(): List<AddedCardsEntity>

    @Query("DELETE from added_cards")
    suspend fun deleteAddedCards()
}