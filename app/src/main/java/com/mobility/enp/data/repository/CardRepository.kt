package com.mobility.enp.data.repository

import android.content.Context
import android.util.Log
import com.mobility.enp.data.model.api_room_models.UserLoginResponseRoomTable
import com.mobility.enp.data.model.cards.registration_croatia.SerialNumberRequest
import com.mobility.enp.data.model.cardsweb.CardWebModel
import com.mobility.enp.data.repository.PassageHistoryRepository.Companion.TAG
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SharedPreferencesHelper
import com.mobility.enp.util.getRedirectWithToken
import com.mobility.enp.util.toTagsForCroatiaUIList
import com.mobility.enp.view.ui_models.TagsForCroatiaUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class CardRepository(database: DRoom, context: Context) : BaseRepository(database, context) {

    fun getLangForCard(context: Context): String {
        return SharedPreferencesHelper.getUserLanguage(context)
    }

    suspend fun getCardData(): Result<CardWebModel> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()
        userToken?.let { token ->
            return try {
                val response = apiService(token).getCreditCardsWeb(getLangKey())
                if (response.isSuccessful) {
                    response.body()?.let { indexData ->
                        Result.success(indexData)
                    } ?: Result.failure(NetworkError.ServerError)
                } else {
                    response.errorBody()?.let { errorBody ->
                        val errorResponse = parseErrorResponse(response.code(), errorBody)
                        Result.failure(NetworkError.ApiError(errorResponse))
                    } ?: Result.failure(NetworkError.ServerError)
                }
            } catch (e: Exception) {
                Log.d(TAG, "getIndexData: ${e.message} ${e.cause}")
                Result.failure(NetworkError.ServerError)
            }
        }

        return Result.failure(NetworkError.ServerError)
    }

    suspend fun setNewPrimaryCard(billId: Int): Result<Unit> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()
        userToken?.let { token ->
            return try {
                val response = apiService(token).cardsSetDefault(billId, getLangKey())
                if (response.isSuccessful) {
                    response.body()?.let { data ->
                        Result.success(data)
                    } ?: Result.failure(NetworkError.ServerError)
                } else {
                    response.errorBody()?.let { errorBody ->
                        val errorResponse = parseErrorResponse(response.code(), errorBody)
                        Result.failure(NetworkError.ApiError(errorResponse))
                    } ?: Result.failure(NetworkError.ServerError)
                }
            } catch (e: Exception) {
                Log.d(TAG, "getIndexData: ${e.message} ${e.cause}")
                Result.failure(NetworkError.ServerError)
            }
        }

        return Result.failure(NetworkError.ServerError)
    }

    suspend fun deleteCard(cardID: String): Result<Unit> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()
        userToken?.let { token ->
            return try {
                val response = apiService(token).deleteCard(cardID)
                if (response.isSuccessful) {
                    response.body()?.let { data ->
                        Result.success(data)
                    } ?: Result.failure(NetworkError.ServerError)
                } else {
                    response.errorBody()?.let { errorBody ->
                        val errorResponse = parseErrorResponse(response.code(), errorBody)
                        Result.failure(NetworkError.ApiError(errorResponse))
                    } ?: Result.failure(NetworkError.ServerError)
                }
            } catch (e: Exception) {
                Log.d(TAG, "getIndexData: ${e.message} ${e.cause}")
                Result.failure(NetworkError.ServerError)
            }

        }
        return Result.failure(NetworkError.ServerError)
    }

    suspend fun getUserTokenData(): UserLoginResponseRoomTable? {
        return withContext(Dispatchers.IO) {
            database.loginDao().fetchAllowedUsers()
        }
    }

    fun isNetAvailable(): Boolean {
        return isNetworkAvailable()
    }

    suspend fun addedPromotionCard(code: String) {
        val user = database.lastUserDao().getLastUser()?.email ?: ""
        if (code == "RS") {
            database.homeCardsDao().enableAdditionForAllExceptRS(user)
        }
        database.homeCardsDao().cardAdded(user, code)
    }

    suspend fun tagsForCroatia(): Result<List<TagsForCroatiaUI>> {
        if (!isNetAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()
        userToken?.let { token ->
            return try {
                val response = apiService(token).getTagsForCroatia(country = "HR")
                if (response.isSuccessful) {
                    response.body()?.let { data ->
                        val tagsList = data.data.tags.toTagsForCroatiaUIList()
                        Result.success(tagsList)
                    } ?: Result.failure(NetworkError.ServerError)
                } else {
                    response.errorBody()?.let { error ->
                        val errorResponse = parseErrorResponse(response.code(), error)
                        Result.failure(NetworkError.ApiError(errorResponse))
                    } ?: Result.failure(NetworkError.ServerError)
                }
            } catch (e: Exception) {
                Log.d("CardRepository", "tagsForCroatia: ${e.message} ${e.cause}")
                Result.failure(NetworkError.ServerError)
            }
        }

        return Result.failure(NetworkError.ServerError)
    }

    suspend fun registrationCroatia(serialNumbers: SerialNumberRequest): Result<String> {
        if (!isNetAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()
        userToken?.let { token ->
            return try {
                val response = apiService(token).postRegistrationCroatia(serialNumbers)
                if (response.isSuccessful) {
                    response.body()?.let { data ->
                        val fullUrl = data.getRedirectWithToken()
                        Result.success(fullUrl)
                    } ?: Result.failure(NetworkError.ServerError)
                } else {
                    response.errorBody()?.let { error ->
                        val errorResponse = parseErrorResponse(response.code(), error)
                        Result.failure(NetworkError.ApiError(errorResponse))
                    } ?: Result.failure(NetworkError.ServerError)
                }
            } catch (e: Exception) {
                Log.d(TAG, "getIndexData: ${e.message} ${e.cause}")
                Result.failure(NetworkError.ServerError)
            }
        }

        return Result.failure(NetworkError.ServerError)
    }

}