package com.mobility.enp.data.repository

import android.content.Context
import android.util.Log
import com.mobility.enp.data.model.api_home_page.HomePageFcmTokenResponse
import com.mobility.enp.data.model.api_room_models.FcmToken
import com.mobility.enp.data.model.api_room_models.UserLanguage
import com.mobility.enp.data.model.api_room_models.UserLoginResponseRoomTable
import com.mobility.enp.data.model.login.LoginBody
import com.mobility.enp.data.model.login.UserResponse
import com.mobility.enp.data.repository.PassageHistoryRepository.Companion.TAG
import com.mobility.enp.data.room.LastUser
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.util.NetworkError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Odgovornost: Upravljanje autentifikacijom i funkcionalnostima za korisnički nalog.
 * Logovanje korisnika, Promena lozinke,Resetovanje lozinke,Deaktivacija korisničkog naloga.
 * Promena jezika
 */

class AuthRepository(database: DRoom, context: Context) : BaseRepository(database, context) {

    /**
     * Language picker
     */
    suspend fun getAllowedUserLanguage(): UserLanguage? {
        return database.languageDao().fetchAllowedUsers()
    }

    suspend fun clearLanguages() {
        database.languageDao().deleteAll()
    }

    suspend fun saveLanguage(language: UserLanguage) {
        database.languageDao().insertLanguage(language)
    }

    suspend fun getLastUser(): LastUser? {
        return withContext(Dispatchers.IO) {
            database.lastUserDao().getLastUser()
        }
    }

    suspend fun storeLastUserEmail(email: String) {
        database.lastUserDao().deleteLastUser()
        database.lastUserDao().upsertLastUser(LastUser(email))
    }

    suspend fun getStoredUser(): UserLoginResponseRoomTable? {
        return withContext(Dispatchers.IO) {
            database.loginDao().fetchAllowedUsers()
        }
    }

    suspend fun writeFcmToken(token: String) {
        database.fcmToken().deleteTable()
        database.fcmToken().insertData(FcmToken(token))
    }

    suspend fun getLanguageKey(): String {
        return withContext(Dispatchers.IO) {
            getLangKey()
        }
    }

    suspend fun insertLoginToken(userLoginResponseRoomTable: UserLoginResponseRoomTable) {
        database.loginDao().deleteAll()
        database.loginDao().insert(userLoginResponseRoomTable)
    }

    suspend fun getUserFcmData(): Pair<String, FcmToken> {
        val userAccessToken = database.loginDao().fetchAllowedUsers().accessToken ?: ""
        val fcmToken = database.fcmToken().getTableData()
        return Pair(userAccessToken, fcmToken)
    }

    suspend fun loginUser(user: LoginBody): Result<UserResponse> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        return try {
            val lang = getLangKey()

            val response = apiService("").getUserLogin(
                lang, user
            )
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

        return Result.failure(NetworkError.ServerError)
    }

    suspend fun postFcmToken(
        fcmToken: FcmToken,
        userToken: String
    ): Result<HomePageFcmTokenResponse> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        return try {
            val response = apiService(userToken).postFirebaseFcmToken(
                fcmToken
            )

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

        return Result.failure(NetworkError.ServerError)
    }
}