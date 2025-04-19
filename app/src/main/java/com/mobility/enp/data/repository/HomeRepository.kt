package com.mobility.enp.data.repository

import android.content.Context
import android.util.Log
import com.mobility.enp.data.model.ProfileImage
import com.mobility.enp.data.model.home.cards.entity.HomeCardsEntity
import com.mobility.enp.data.model.home.relation.HomeWithDetails
import com.mobility.enp.data.model.home.response.Data
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.toEntityList

/**
 * Odgovornost: Upravljanje podacima i funkcijama za početni ekran aplikacije.
 */

class HomeRepository(
    database: DRoom,
    context: Context,
) : BaseRepository(database, context) {

    /**
     * Home Screen Data GET
     */
    suspend fun getHomeDataFromServer(): Result<HomeWithDetails> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()
        userToken?.let { token ->
            try {
                val lang = getLangKey()
                val remoteData = apiService(token).geHomeScreenData(lang)

                if (remoteData.isSuccessful) {
                    remoteData.body()?.let { responseBody ->
                        saveAllHomeData(responseBody.data)

                        val localData = getLocalAllHomeData()
                        return if (localData != null) {
                            Result.success(localData)
                        } else {
                            Result.failure(NetworkError.ServerError)
                        }
                    } ?: return Result.failure(NetworkError.ServerError)
                } else {
                    remoteData.errorBody()?.let { errorBody ->
                        val apiErrorResponse = parseErrorResponse(remoteData.code(), errorBody)
                        return Result.failure(NetworkError.ApiError(apiErrorResponse))
                    } ?: return Result.failure(NetworkError.ServerError)
                }
            } catch (e: Exception) {
                return Result.failure(NetworkError.ServerError)
            }
        }

        return Result.failure(NetworkError.ServerError)
    }


    private suspend fun saveAllHomeData(data: Data) {
        val homeDao = database.homeScreenDao()

        try {
            homeDao.insertHome(data.toHomeEntity())
            homeDao.insertTollHistory(data.toHomeTollHistory(homeId = 1))
            homeDao.insertInvoices(data.toHomeInvoices(homeId = 1))
            homeDao.insertInvoiceCurrencies(data.toHomeInvoiceCurrencies(invoiceId = 1))
            Log.d("HomeScreen Database", "Svi podaci uspešno sačuvani")
        } catch (e: Exception) {
            Log.e(
                "HomeRepository getLocalAllHomeData",
                "Greška pri čuvanju podataka: ${e.message}",
                e
            )
        }
    }

    suspend fun getLocalAllHomeData(): HomeWithDetails? {
        return database.homeScreenDao().getHomeWithDetails()
    }

    /**
     * Home Profile Picture
     */
    suspend fun getProfileImage(displayName: String): ProfileImage? {
        return database.profileImageDao().getProfileImage(displayName)
    }

    /**
     * Home Cards GET
     */
    suspend fun getCardsFromServer(): Result<List<HomeCardsEntity>> {
        val userToken = getUserToken() ?: return Result.failure(NetworkError.NoConnection)

        val user = getUserForPromotion()
        val localCards = getHomeCards(user)
        if (localCards.isNotEmpty()) {
            return Result.success(localCards)
        }

        return try {
            val lang = getLangKey()
            val remoteData = apiService(userToken).getCreditCardsWeb(lang)
            if (remoteData.isSuccessful) {
                remoteData.body()?.let { responseBody ->
                    val cardsEntities = responseBody.toEntityList(context, user)
                    saveHomeCards(cardsEntities)

                    Result.success(cardsEntities)
                } ?: Result.failure(NetworkError.ServerError)
            } else {
                Result.failure(NetworkError.ServerError)
            }
        } catch (e: Exception) {
            Log.e("HomeRepository getCards", "Greška pri preuzimanju kartica: ${e.message}", e)
            Result.failure(NetworkError.ServerError)
        }
    }

    suspend fun getHomeCards(user: String): List<HomeCardsEntity> {
        return database.homeCardsDao().getHomeCardsList(user)
    }

    suspend fun getUserForPromotion(): String {
        return database.lastUserDao().getLastUser().email
    }

    private suspend fun saveHomeCards(cards: List<HomeCardsEntity>) {
        database.homeCardsDao().insertHomeCards(cards)
    }

    suspend fun updateHomeCard(card: HomeCardsEntity) {
        database.homeCardsDao().updatePromotionCard(card)
    }

}




