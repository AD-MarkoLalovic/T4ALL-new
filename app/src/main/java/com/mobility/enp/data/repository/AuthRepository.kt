package com.mobility.enp.data.repository

import android.content.Context
import android.util.Log
import com.mobility.enp.R
import com.mobility.enp.data.model.api_home_page.HomePageFcmTokenResponse
import com.mobility.enp.data.model.api_my_profile.ChangePasswordRequest
import com.mobility.enp.data.model.api_room_models.FcmToken
import com.mobility.enp.data.model.api_room_models.UserLoginResponseRoomTable
import com.mobility.enp.data.model.login.CustomerSupport
import com.mobility.enp.data.model.login.ForgotPasswordRequest
import com.mobility.enp.data.model.login.LoginBody
import com.mobility.enp.data.model.login.UserResponse
import com.mobility.enp.data.model.registration.RegistrationCountry
import com.mobility.enp.data.repository.PassageHistoryRepository.Companion.TAG
import com.mobility.enp.data.room.LastUser
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.util.NetworkError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Odgovornost: Upravljanje autentifikacijom i funkcionalnostima za korisnički nalog.
 * Logovanje korisnika, Promena lozinke,Resetovanje lozinke,Deaktivacija korisničkog naloga.
 * Promena jezika> Registracija korisnika
 */

class AuthRepository(database: DRoom, context: Context) : BaseRepository(database, context) {

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

    fun netStateAvailable(): Boolean {
        return isNetworkAvailable()
    }

    suspend fun insertLoginToken(userLoginResponseRoomTable: UserLoginResponseRoomTable) {
        database.loginDao().deleteAll()
        database.loginDao().insert(userLoginResponseRoomTable)
    }

    suspend fun getUserFcmData(): Pair<String, FcmToken?> {
        val userAccessToken = database.loginDao().fetchAllowedUsers()?.accessToken ?: ""
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
    }


    suspend fun postForgotPassword(
        email: ForgotPasswordRequest,
    ): Result<Boolean> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        return try {
            val response = apiService("").forgotPassword(email)

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
            Result.failure(NetworkError.ServerError)
        }
    }

    suspend fun sendLangKey(): Result<Unit> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()

        userToken?.let { token ->
            return try {
                val sendKeyResponse = apiService(token).changeLanguage(getLangKey())
                if (sendKeyResponse.isSuccessful) {
                    sendKeyResponse.body()?.let { response ->
                        Result.success(response)
                    } ?: Result.failure(NetworkError.ServerError)
                } else {
                    sendKeyResponse.errorBody()?.let { errorBody ->
                        val apiErrorResponse = parseErrorResponse(sendKeyResponse.code(), errorBody)
                        Result.failure(NetworkError.ApiError(apiErrorResponse))
                    } ?: Result.failure(NetworkError.ServerError)
                }
            } catch (e: Exception) {
                Log.e("LANG_KEY", "unexpected error: ${e.message}", e)
                Result.failure(NetworkError.ServerError)
            }
        }

        return Result.failure(NetworkError.ServerError)
    }


    suspend fun getToken(): UserLoginResponseRoomTable? {
        return withContext(Dispatchers.IO) {
            database.loginDao().fetchAllowedUsers()
        }
    }

    /**
     * Registration
     */
    fun getCountries(context: Context): List<RegistrationCountry> {
        return listOf(
            RegistrationCountry(
                context.getString(R.string.serbia_country_code),
                context.getString(R.string.serbia),
                R.drawable.serbia_flag
            ),
            RegistrationCountry(
                context.getString(R.string.macedonia_country_code),
                context.getString(R.string.macedonia),
                R.drawable.macedonia_flag
            ),
            RegistrationCountry(
                context.getString(R.string.montenegro_country_code),
                context.getString(R.string.montenegro),
                R.drawable.montenegro_flag
            )
        )
    }

    suspend fun passwordChange(body: ChangePasswordRequest): Result<Unit> {
        if (!isNetworkAvailable()) return Result.failure(NetworkError.NoConnection)

        val userToken = getUserToken() ?: return Result.failure(NetworkError.ServerError)

        return try {
            val lang = getLangKey()
            val response = apiService(userToken).putChangePassword(body, lang)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorResponse = response.errorBody()?.let {
                    parseErrorResponse(errorCode = response.code(), errorBody = it)
                }
                Result.failure(errorResponse?.let { NetworkError.ApiError(it) }
                    ?: NetworkError.ServerError)
            }
        } catch (e: Exception) {
            Log.d("PasswordChange", "AuthRepository: ${e.message} ${e.cause}")
            Result.failure(NetworkError.ServerError)
        }
    }

    suspend fun sendCustomerSupport(data: CustomerSupport): Result<Unit> {
        if (!isNetworkAvailable()) return Result.failure(NetworkError.NoConnection)

        return try {
            val lang = getLangKey()
            val response = apiService("").sendCustomerSupport(lang, data)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorResponse = response.errorBody()?.let {
                    parseErrorResponse(errorCode = response.code(), errorBody = it)
                }
                Result.failure(errorResponse?.let { NetworkError.ApiError(it) }
                    ?: NetworkError.ServerError)
            }
        } catch (e: Exception) {
            Log.d("SendCustomerSupport", "AuthRepository: ${e.message} ${e.cause}")
            Result.failure(NetworkError.ServerError)
        }
    }
}