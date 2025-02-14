package com.mobility.enp.data.repository

import android.content.Context
import android.util.Log
import com.mobility.enp.data.model.home.relation.HomeWithDetails
import com.mobility.enp.data.model.home.response.HomeResponse
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.util.NetworkError

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
        Log.d("HomeScreen", "getHomeDataFromServer() - Start")

        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()
        Log.d("HomeScreen", "getHomeDataFromServer() - User token retrieved: $userToken")

        userToken?.let { token ->
            try {
                val lang = getLangKey()
                Log.d("HomeScreen", "getHomeDataFromServer() - Language key: $lang")

                val remoteData = apiService(token).geHomeScreenData(lang)
                Log.d("HomeScreen", "getHomeDataFromServer() - API response received: ${remoteData.code()}")

                if (remoteData.isSuccessful) {
                    remoteData.body()?.let { responseBody ->
                        Log.d("HomeScreen", "getHomeDataFromServer() - API response body is not null, saving data")

                        saveAllHomeData(responseBody)

                        val localData = getLocalAllHomeData()
                        return if (localData != null) {
                            Log.d("HomeScreen", "getHomeDataFromServer() - Local data retrieved successfully")
                            Result.success(localData)
                        } else {
                            Log.e("HomeScreen", "getHomeDataFromServer() - Local data is null after saving")
                            Result.failure(NetworkError.ServerError)
                        }
                    } ?: run {
                        Log.e("HomeScreen", "getHomeDataFromServer() - API response body is null")
                        return Result.failure(NetworkError.ServerError)
                    }
                } else {
                    Log.e("HomeScreen", "getHomeDataFromServer() - API request failed with code: ${remoteData.code()}")

                    remoteData.errorBody()?.let { errorBody ->
                        val apiErrorResponse = parseErrorResponse(remoteData.code(), errorBody)
                        Log.e("HomeScreen", "getHomeDataFromServer() - API error response: $apiErrorResponse")
                        return Result.failure(NetworkError.ApiError(apiErrorResponse))
                    } ?: run {
                        Log.e("HomeScreen", "getHomeDataFromServer() - API error body is null")
                        return Result.failure(NetworkError.ServerError)
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeScreen", "getHomeDataFromServer() - Unexpected error: ${e.message}", e)
                return Result.failure(NetworkError.ServerError)
            }
        }

        Log.e("HomeScreen", "getHomeDataFromServer() - User token is null")
        return Result.failure(NetworkError.ServerError)
    }



    private suspend fun saveAllHomeData(response: HomeResponse) {
        val dao = database.homeScreenDao()

        // 1. Unesi Home entitet u bazu i dobij ID
        Log.d("HomeScreen", "Starting to insert Home entity into the database.")
        val homeEntity = response.data.toHomeEntity()
        Log.d("HomeScreen", "Home entity to be inserted: $homeEntity")
        val homeId = dao.insertHomeManual(homeEntity).toInt()
        Log.d("HomeScreen", "Inserted Home entity with homeId: $homeId.")

        // 2. Mapiraj i unesi TollHistory u bazu
        Log.d("HomeScreen", "Mapping and inserting TollHistory for homeId: $homeId.")
        val tollHistory = response.data.toHomeTollHistory(1)
        Log.d("HomeScreen", "TollHistory to be inserted: $tollHistory")
        dao.insertTollHistory(tollHistory)

        // 3. Unesi stavke u InvoiceHomeEntity i dobij ID-eve
        val invoiceList = response.data.toHomeInvoices(1)
        Log.d("HomeScreen", "Invoice list to be inserted: $invoiceList")
        val invoiceIds = dao.insertInvoice(invoiceList) // Ovo vraća listu ID-eva
        Log.d("HomeScreen", "Invoice entities inserted. invoiceIds: $invoiceIds")

        // 4. Proveri da li su ID-evi validni
        if (invoiceIds.isNotEmpty()) {
            Log.d("HomeScreen", "Invoice IDs are valid. Proceeding with InvoiceHomeTotalCurrencyEntity insertion.")

            // 5. Mapiraj za svaki InvoiceHomeEntity u InvoiceHomeTotalCurrencyEntity
            invoiceList.forEachIndexed { index, invoice ->
                val invoiceId = invoiceIds[index].toInt() // Dobijamo ID iz prethodnih ubačenih stavki
                Log.d("HomeScreen", "Processing Invoice with invoiceId: $invoiceId.")

                // 6. Mapiraj u odgovarajući entitet
                val invoiceCurrencies = response.data.toHomeInvoiceCurrencies(invoiceId)
                Log.d("HomeScreen", "Mapped InvoiceCurrencies for invoiceId: $invoiceId. Currency list size: ${invoiceCurrencies.size}")

                // 7. Sačuvaj stavke u bazu
                if (invoiceCurrencies.isNotEmpty()) {
                    Log.d("HomeScreen", "Inserting InvoiceCurrencies for invoiceId: $invoiceId into the database.")
                    dao.insertInvoiceCurrency(invoiceCurrencies)  // Sačuvaj stavke u povezanoj tabeli
                    Log.d("HomeScreen", "InvoiceCurrencies inserted successfully for invoiceId: $invoiceId.")
                } else {
                    Log.e("HomeScreen", "No currencies found for invoiceId: $invoiceId")
                }
            }
        } else {
            Log.e("HomeScreen", "No invoice IDs were returned from insertInvoice.")
        }
    }




    suspend fun getLocalAllHomeData(): HomeWithDetails? {
        return database.homeScreenDao().getHomeWithDetails()
    }

}

