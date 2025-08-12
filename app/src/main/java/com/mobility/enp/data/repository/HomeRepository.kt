package com.mobility.enp.data.repository

import android.content.Context
import android.util.Log
import com.mobility.enp.data.model.ProfileImage
import com.mobility.enp.data.model.cardsweb.CardWebModel
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
        if (!isNetworkAvailable()) return Result.failure(NetworkError.NoConnection)

        val userToken = getUserToken() ?: return Result.failure(NetworkError.ServerError)

        return try {
            val lang = getLangKey()
            val remoteData = apiService(userToken).geHomeScreenData(lang)

            if (remoteData.isSuccessful) {
                remoteData.body()?.let { responseBody ->
                    saveAllHomeData(responseBody.data)

                    val localData = getLocalAllHomeData()
                    if (localData != null) {
                        Result.success(localData)
                    } else {
                        Result.failure(NetworkError.ServerError)
                    }
                } ?: Result.failure(NetworkError.ServerError)
            } else {
                val errorResponse = remoteData.errorBody()?.let {
                    parseErrorResponse(errorCode = remoteData.code(), errorBody = it)
                }
                Result.failure(errorResponse?.let { NetworkError.ApiError(it) }
                    ?: NetworkError.ServerError)
            }
        } catch (e: Exception) {
            Log.d("getHomeDataFromServer", "HomeRepository: ${e.message} ${e.cause}")
            Result.failure(NetworkError.ServerError)
        }


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

        return try {
            val user = getUserForPromotion()
            val lang = getLangKey()
            val remoteData = apiService(userToken).getCreditCardsWeb(lang)
            if (remoteData.isSuccessful) {
                remoteData.body()?.let { responseBody ->
                    saveHomeCards(context, user, responseBody)

                    Result.success(getHomeCards(user))
                } ?: Result.failure(NetworkError.ServerError)
            } else {
                Result.failure(NetworkError.ServerError)
            }
        } catch (e: Exception) {
            Log.e("HomeRepository getCards", "Greška pri preuzimanju kartica: ${e.message}", e)
            Result.failure(NetworkError.ServerError)
        }
    }

    private suspend fun saveHomeCards(context: Context, user: String, model: CardWebModel) {
        val newList = model.toEntityList(context, user)
        val existingList = database.homeCardsDao().getCardsByUser(user)

        val newCodes = newList.map { it.code }.toSet()

        // Briši sve lokalne kartice koje su dodate preko web-a
        val toDelete = existingList.filter { it.code !in newCodes }
        database.homeCardsDao().deleteCards(toDelete)

        // Dodaj nove kartice
        database.homeCardsDao().insertHomeCards(newList)
    }

    suspend fun getHomeCards(user: String): List<HomeCardsEntity> {
        return database.homeCardsDao().getHomeCardsList(user)
    }

    suspend fun getUserForPromotion(): String {
        return database.lastUserDao().getLastUser()?.email ?:""
    }

    suspend fun updateHomeCard(card: HomeCardsEntity) {
        database.homeCardsDao().updatePromotionCard(card)
    }

}




