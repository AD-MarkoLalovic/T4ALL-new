package com.mobility.enp.data.repository

import android.content.Context
import android.util.Log
import com.mobility.enp.data.model.ProfileImage
import com.mobility.enp.data.model.api_my_profile.SupportRequest
import com.mobility.enp.data.model.api_my_profile.basic_information.response.BasicInfoResponse
import com.mobility.enp.data.model.api_room_models.FcmToken
import com.mobility.enp.data.model.deactivation.DeactivateAccountModel
import com.mobility.enp.data.repository.PassageHistoryRepository.Companion.TAG
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.toTagUiModel
import com.mobility.enp.view.ui_models.my_tags.TagUiModel
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
                Log.d(TAG, "error: ${e.message} ${e.cause}")
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
                Log.d(TAG, "error: ${e.message} ${e.cause}")
                Result.failure(NetworkError.ServerError)
            }
        }

        return Result.failure(NetworkError.ServerError)
    }

    suspend fun getBasicUserInformation(): Result<BasicInfoResponse> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()
        userToken?.let { token ->
            return try {
                val response = apiService(token).getUserData()
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
                Log.d(TAG, "error: ${e.message} ${e.cause}")
                Result.failure(NetworkError.ServerError)
            }
        }

        return Result.failure(NetworkError.ServerError)
    }

    suspend fun sendSupportMessage(request: SupportRequest): Result<Boolean> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()
        userToken?.let { token ->
            return try {
                val response = apiService(token).sendContactMessage(request)
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
                Log.d(TAG, "error: ${e.message} ${e.cause}")
                Result.failure(NetworkError.ServerError)
            }
        }

        return Result.failure(NetworkError.ServerError)
    }


    suspend fun postDeactivateAccount(pair: Pair<String, String>): Result<DeactivateAccountModel> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()
        userToken?.let { token ->
            return try {
                val response = apiService(token).deactivateAccount(
                    getLangKey(),
                    pair.first,
                    pair.second,
                    "127.0.0.1"
                )
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
                Log.d(TAG, "error: ${e.message} ${e.cause}")
                Result.failure(NetworkError.ServerError)
            }
        }

        return Result.failure(NetworkError.ServerError)
    }

    suspend fun getMyTags(): Result<List<TagUiModel>> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken() ?: return Result.failure(NetworkError.ServerError)

        return try {
            val lang = getLangKey()
            val response = apiService(userToken).getUserTagsNew(1, 2000, lang)

            if (response.isSuccessful) {
                val tags = response.body()?.data?.tags?.items?.toTagUiModel().orEmpty()
                Result.success(tags)
            } else {
                val errorResponse =
                    response.errorBody()?.let { parseErrorResponse(response.code(), it) }
                Result.failure(errorResponse?.let { NetworkError.ApiError(it) }
                    ?: NetworkError.ServerError)
            }

        } catch (e: Exception) {
            Log.d("MyTags", "ProfileRepository: ${e.message} ${e.cause}")
            Result.failure(NetworkError.ServerError)
        }
    }

    suspend fun addTag(serialNumber: String, verificationCode: String): Result<Unit> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }
        val userToken = getUserToken() ?: return Result.failure(NetworkError.ServerError)

        return try {
            val lang = getLangKey()
            val response = apiService(userToken).postAddTag(
                serialNumber = serialNumber,
                verificationCode = verificationCode,
                lang
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorResponse =
                    response.errorBody()?.let { parseErrorResponse(response.code(), it) }
                Result.failure(errorResponse?.let { NetworkError.ApiError(it) }
                    ?: NetworkError.ServerError)
            }
        } catch (e: Exception) {
            Log.d("AddTag", "ProfileRepository: ${e.message} ${e.cause}")
            Result.failure(NetworkError.ServerError)
        }
    }

    suspend fun reportLostTag(serialNumber: String): Result<Unit> {
        if (!isNetworkAvailable()) return Result.failure(NetworkError.NoConnection)

        val userToken = getUserToken() ?: return Result.failure(NetworkError.ServerError)

        return try {
            val response = apiService(userToken).postLostTag(serialNumber = serialNumber)
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
            Log.d("LostTag", "ProfileRepository: ${e.message} ${e.cause}")
            Result.failure(NetworkError.ServerError)
        }
    }

    suspend fun reportFoundTag(serialNumber: String): Result<Unit> {
        if (!isNetworkAvailable()) return Result.failure(NetworkError.NoConnection)

        val userToken = getUserToken() ?: return Result.failure(NetworkError.ServerError)

        return try {
            val response = apiService(userToken).postFoundTag(serialNumber = serialNumber)
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
            Log.d("FoundTag", "ProfileRepository: ${e.message} ${e.cause}")
            Result.failure(NetworkError.ServerError)
        }
    }

}