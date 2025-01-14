package com.mobility.enp.data.repository

import android.content.Context
import android.util.Log
import com.mobility.enp.data.model.api_my_profile.refund_request.SendRefundRequest
import com.mobility.enp.data.model.api_my_profile.refund_request.entity.DataRefundRequestEntity
import com.mobility.enp.data.model.api_my_profile.refund_request.response.RefundRequestsResponse
import com.mobility.enp.data.model.api_my_profile.refund_request.tags.entity.TagsRefundRequestEntity
import com.mobility.enp.data.model.api_my_profile.refund_request.tags.response.TagsResponseRefundRequest
import com.mobility.enp.data.model.banks.entity.BanksEntity
import com.mobility.enp.data.model.banks.response.BanksResponse
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.util.NetworkError

/**
 * Odgovornost: Upravljanje podacima korisnika, uključujući profilne informacije, podesavanje
 * apikacije, pristup računima, kao i funkcionalnosti poput slanja pomoći i refundacija.
 * Dobavljanje informacija o korisničkim tagovima.Odjava korisnika
 */

class UserRepository(
    database: DRoom,
    context: Context,
) : BaseRepository(database, context) {


    /**
     * Refund Request GET
     */

    suspend fun getRefundRequestFromServer(): Result<List<DataRefundRequestEntity>> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()
        userToken?.let { token ->
            val langKey = getLangKey()

            return try {
                val remoteData = apiService(token).refundRequest(langKey)
                if (remoteData.isSuccessful) {
                    remoteData.body()?.let { responseBody ->
                        // Sačuvam podatke iz API-ja u lokalnu bazu
                        saveRefundRequests(responseBody)
                        Result.success(getLocalRefundRequests())
                    } ?: Result.failure(NetworkError.ServerError)
                } else {
                    remoteData.errorBody()?.let { errorBody ->
                        val apiErrorResponse = parseErrorResponse(errorBody)
                        Result.failure(NetworkError.ApiError(apiErrorResponse))
                    } ?: Result.failure(NetworkError.ServerError)
                }
            } catch (e: Exception) {
                // Ovde obrađujem neočekivane greške (mrežni problemi, timeout, itd.)
                Log.e("RefundRequest", "Neočekivana greška: ${e.message}", e)
                Result.failure(NetworkError.ServerError)
            }
        }

        // Ako nema korisničkog tokena vraćam grešku
        return Result.failure(NetworkError.ServerError)
    }


    private suspend fun saveRefundRequests(refundRequest: RefundRequestsResponse) {
        val entities = refundRequest.toEntityList()
        database.refundRequestDao().insertRefundRequest(entities)
    }

    suspend fun getLocalRefundRequests(): List<DataRefundRequestEntity> {
        return database.refundRequestDao().getAllRefundRequest()
    }

    suspend fun getTagsRefundRequest(): Result<List<TagsRefundRequestEntity>> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()
        userToken?.let { token ->
            return try {
                val response = apiService(token).getTagsRefundRequest( 100)

                if (response.isSuccessful) {
                    response.body()?.let { tagsData ->
                        saveTagsRefundRequest(tagsData)
                        Result.success(getLocalTagsRefundRequest()) // Uspešan rezultat
                    } ?: Result.failure(NetworkError.ServerError) // Ako telo odgovora nije validno
                } else {
                    response.errorBody()?.let { errorBody ->
                        val apiErrorResponse = parseErrorResponse(errorBody)
                        Result.failure(NetworkError.ApiError(apiErrorResponse))
                    } ?: Result.failure(NetworkError.ServerError)
                }
            } catch (e: Exception) {
                Log.e("TagsRefundRequest", "Neočekivana greška: ${e.message}", e)
                Result.failure(NetworkError.ServerError)
            }
        }

        return Result.failure(NetworkError.ServerError) // Ako token nije pronađen
    }

    private suspend fun saveTagsRefundRequest(tagsResponse: TagsResponseRefundRequest) {
        val tagsEntity = tagsResponse.data.toTagsEntityList().filterNotNull()
        database.tagsRefundRequest().insertTagsRefundRequest(tagsEntity)
    }

    suspend fun getLocalTagsRefundRequest(): List<TagsRefundRequestEntity> {
        return database.tagsRefundRequest().getTagsRefundRequest()
    }

    /**
     * Refund Request POST
     */

    suspend fun submitRefundRequest(refundRequest: SendRefundRequest): Result<Unit> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()
        userToken?.let { token ->
            return try {
                val lang = getLangKey()
                val response = apiService(token).sendRefundRequest(lang, refundRequest)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    response.errorBody()?.let { errorBody ->
                        val apiErrorResponse = parseErrorResponse(errorBody)
                        Result.failure(NetworkError.ApiError(apiErrorResponse))
                    } ?: Result.failure(NetworkError.ServerError)
                }
            } catch (e: Exception) {
                Log.e("submitRefundRequest", "Neočekivana greška: ${e.message}", e)
                Result.failure(NetworkError.ServerError)
            }
        }
        return Result.failure(NetworkError.ServerError)
    }


    /**
     * Banks
     */

    suspend fun getBanksFromServer(): Result<List<BanksEntity>> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()

        userToken?.let { token ->
            return try {
                val remoteBanks = apiService(token).getBanks()
                if (remoteBanks.isSuccessful) {
                    remoteBanks.body()?.let { banksResponse ->
                        saveListBanks(banksResponse)
                        Result.success(getLocalBanks())
                    } ?: Result.failure(NetworkError.ServerError)
                } else {
                    remoteBanks.errorBody()?.let { errorBody ->
                        val apiErrorResponse = parseErrorResponse(errorBody)
                        Result.failure(NetworkError.ApiError(apiErrorResponse))
                    } ?: Result.failure(NetworkError.ServerError)
                }
            } catch (e: Exception) {
                Log.e("Banks", "Neočekivana greška: ${e.message}", e)
                Result.failure(NetworkError.ServerError)
            }
        }

        return Result.failure(NetworkError.ServerError)
    }

    private suspend fun saveListBanks(banks: BanksResponse) {
        val banksEntity = banks.data.results.toListBanksEntity()
        database.bankDao().insertBanks(banksEntity)
    }

    suspend fun getLocalBanks(): List<BanksEntity> {
        return database.bankDao().getAllBanks()
    }

    /**
     * Send language
     */

    suspend fun sendLangKey() {
        val userToken = getUserToken()

        userToken?.let { token ->
            apiService(token).changeLanguage(getLangKey())
        }
    }

}