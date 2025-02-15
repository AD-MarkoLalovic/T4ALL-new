package com.mobility.enp.data.repository

import android.content.Context
import android.util.Log
import com.mobility.enp.data.model.home.relation.HomeWithDetails
import com.mobility.enp.data.model.home.response.Data
import com.mobility.enp.data.model.home.response.HomeResponse
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.util.NetworkError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
            Log.e("HomeScreen Database", "Greška pri čuvanju podataka: ${e.message}", e)
        }
    }


    suspend fun getLocalAllHomeData(): HomeWithDetails? {
        return try {
                Log.d("HomeScreen Database", "Dohvatam podatke iz baze...")
                val data = database.homeScreenDao().getHomeWithDetails()
                Log.d("HomeScreen Database", "Podaci preuzeti: $data")
                data
            } catch (e: Exception) {
                Log.e("HomeScreen Database", "Greška pri dohvaćanju podataka: ${e.message}", e)
                null
            }
    }

}

