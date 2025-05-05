package com.mobility.enp.data.room.api_related_daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mobility.enp.data.model.home.cards.entity.HomeCardsEntity

@Dao
interface HomeCardsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHomeCards(cards: List<HomeCardsEntity>)

    @Update
    suspend fun updatePromotionCard(cad: HomeCardsEntity)

    @Query("SELECT * FROM home_cards WHERE email = :userEmail")
    suspend fun getHomeCardsList(userEmail: String): List<HomeCardsEntity>

    @Query("DELETE FROM home_cards WHERE email = :email AND code = :code")
    suspend fun cardAdded(email: String, code: String)

    @Query("UPDATE home_cards SET additionEnabled = 1 WHERE email = :email AND code != 'RS'")
    suspend fun enableAdditionForAllExceptRS(email: String)

    @Query("SELECT * FROM home_cards WHERE email = :email")
    suspend fun getCardsByUser(email: String): List<HomeCardsEntity>

    @Delete
    suspend fun deleteCards(cards: List<HomeCardsEntity>)



}