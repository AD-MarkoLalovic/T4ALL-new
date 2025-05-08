package com.mobility.enp.data.repository

import android.content.Context
import android.util.Log
import com.mobility.enp.data.model.ProfileImage
import com.mobility.enp.data.model.api_home_page.HomePageFcmTokenResponse
import com.mobility.enp.data.model.api_room_models.FcmToken
import com.mobility.enp.data.model.cardsweb.CardWebModel
import com.mobility.enp.data.repository.PassageHistoryRepository.Companion.TAG
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.util.NetworkError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//changed this to be for profile fragment because it was not used initial idea

class ProfileRepository(database: DRoom, context: Context) : BaseRepository(database, context) {

    suspend fun userToken(): String? {
        return getUserToken()
    }

    fun getLanguageKey(): String {
        return getLangKey()
    }

    suspend fun deleteDatabase() {
        withContext(Dispatchers.IO) {
            database.clearAllData()
        }
    }

    suspend fun getFcmData(): FcmToken? {
        return withContext(Dispatchers.IO) {
            database.fcmToken().getTableData()
        }
    }

    suspend fun deleteProfilePicture() {
        database.profileImageDao().deleteAll()
    }

    suspend fun getStoredImage(): List<ProfileImage>? {
        return withContext(Dispatchers.IO) {
            database.profileImageDao().selectAll()
        }
    }

    fun isNetworkAvail(): Boolean {
        return isNetworkAvailable()
    }

    suspend fun deleteFirebaseToken(fcmToken: String): Result<Boolean> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()
        userToken?.let { token ->
            return try {
                val response = apiService(token).deleteFirebaseToken(fcmToken)
                if (response.isSuccessful) {
                    response.body()?.let { _ ->
                        Result.success(true)
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

    suspend fun logoutUser(): Result<Boolean> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()
        userToken?.let { token ->
            return try {
                val response = apiService(token).postLogoutUser()
                if (response.isSuccessful) {
                    response.body()?.let { _ ->
                        Result.success(true)
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

}