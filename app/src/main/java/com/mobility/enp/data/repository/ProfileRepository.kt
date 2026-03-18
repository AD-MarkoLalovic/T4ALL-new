package com.mobility.enp.data.repository

import android.content.Context
import android.util.Log
import com.mobility.enp.data.model.ProfileImage
import com.mobility.enp.data.model.api_my_profile.SupportRequest
import com.mobility.enp.data.model.api_my_profile.basic_information.entity.BasicInfoEntity
import com.mobility.enp.data.model.api_my_profile.basic_information.response.BasicInfoResponse
import com.mobility.enp.data.model.api_my_profile.my_tags.response.Pagination
import com.mobility.enp.data.model.api_room_models.FcmToken
import com.mobility.enp.data.model.api_tags.ActivateDeactivateTagModel
import com.mobility.enp.data.model.deactivation.DeactivateAccountModel
import com.mobility.enp.data.model.home.relation.HomeWithDetails
import com.mobility.enp.data.repository.PassageHistoryRepository.Companion.TAG
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SharedPreferencesHelper
import com.mobility.enp.util.toTagUiModel
import com.mobility.enp.view.ui_models.my_tags.TagUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
        database.clearAllData()
    }

    suspend fun getFcmData(): FcmToken? {
        return withContext(Dispatchers.IO) {
            database.fcmToken().getTableData()
        }
    }

    suspend fun getLocalAllHomeData(): HomeWithDetails? {
        return database.homeScreenDao().getHomeWithDetails()
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

        val userToken = getUserToken() ?: return Result.failure(NetworkError.ServerError)

        return try {
            val response = apiService(userToken).getUserData()
            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    SharedPreferencesHelper.saveUserCountryCode(context, responseBody.data.country.code)
                    val entity = responseBody.data.toEntity()
                    database.basicInfoDao().insertBasicInfo(entity)
                    Result.success(responseBody)
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

    suspend fun getMyTags(country: String, perPage: Int): Result<List<TagUiModel>> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken() ?: return Result.failure(NetworkError.ServerError)

        return try {
            val lang = getLangKey()
            val response = apiService(userToken).getUserTagsNewByCountry(1, perPage, lang, country)

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


    suspend fun getAllMyTagsByCountry(
        countryCode: String,
        perPage: Int, page: Int
    ): Result<Pair<List<TagUiModel>, Pagination?>> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken() ?: return Result.failure(NetworkError.ServerError)

        return try {
            val lang = getLangKey()
            val response =
                apiService(userToken).getUserTagsNewByCountry(page, perPage, lang, countryCode)

            if (response.isSuccessful) {
                val tags = response.body()?.data?.tags?.items?.toTagUiModel().orEmpty()
                val pagination = response.body()?.data?.tags?.pagination
                Result.success(Pair(tags, pagination))
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


    suspend fun getAllMyTagsBySerialNumber(
        countryCode: String,
        serialNumber: String
    ): Result<Pair<List<TagUiModel>, Pagination?>> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken() ?: return Result.failure(NetworkError.ServerError)

        return try {
            val lang = getLangKey()
            val response =
                apiService(userToken).getUserTagsNewBySerialNumber(lang, serialNumber, countryCode)

            if (response.isSuccessful) {
                val tags = response.body()?.data?.tags?.items?.toTagUiModel().orEmpty()
                val pagination = response.body()?.data?.tags?.pagination
                Result.success(Pair(tags, pagination))
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

    //the better api call for normal users
    suspend fun getAllMyTagsByCountry(
        countryCode: String,
        perPage: Int
    ): Result<List<TagUiModel>> {
        if (!isNetworkAvailable()) return Result.failure(NetworkError.NoConnection)

        val userToken = getUserToken() ?: return Result.failure(NetworkError.ServerError)
        val lang = getLangKey()

        return try {
            // Fetch first page
            val baseResponse = apiService(userToken)
                .getUserTagsNewByCountry(1, perPage, lang, countryCode)

            if (!baseResponse.isSuccessful) {
                val errorResponse =
                    baseResponse.errorBody()?.let { parseErrorResponse(baseResponse.code(), it) }
                return Result.failure(errorResponse?.let { NetworkError.ApiError(it) }
                    ?: NetworkError.ServerError)
            }

            val body = baseResponse.body()!!
            val pagination = body.data?.tags?.pagination
            val itemsPage1 = body.data?.tags?.items?.toTagUiModel().orEmpty()
            val lastPage = pagination?.lastPage ?: 1

            // If only one page (no more tag data) → return immediately
            if (lastPage == 1) {
                return Result.success(itemsPage1)
            }

            // else fetch remaining pages in parallel using async
            val deferredPages = (2..lastPage).map { page ->
                coroutineScope {
                    async {
                        val pageResponse = apiService(userToken)
                            .getUserTagsNewByCountry(page, perPage, lang, countryCode)

                        if (pageResponse.isSuccessful) {
                            pageResponse.body()?.data?.tags?.items?.toTagUiModel().orEmpty()
                        } else {
                            emptyList()
                        }
                    }
                }
            }

            val remainingItems = deferredPages.awaitAll().flatten()
            val allItems = itemsPage1 + remainingItems

            Result.success(allItems)

        } catch (e: Exception) {
            Result.failure(NetworkError.ServerError)
        }
    }

    suspend fun deactivateTag(body: ActivateDeactivateTagModel): Result<Unit> {
        if (!isNetworkAvailable()) return Result.failure(NetworkError.NoConnection)

        val userToken = getUserToken() ?: return Result.failure(NetworkError.ServerError)

        return try {
            val response = apiService(userToken).deactivateTag(body = body)
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
            Log.d("DeactivateTag", "ProfileRepository: ${e.message} ${e.cause}")
            Result.failure(NetworkError.ServerError)
        }
    }


    suspend fun activateTag(body: ActivateDeactivateTagModel): Result<Unit> {
        if (!isNetworkAvailable()) return Result.failure(NetworkError.NoConnection)

        val userToken = getUserToken() ?: return Result.failure(NetworkError.ServerError)

        return try {
            val response = apiService(userToken).activateTag(body = body)
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
            Log.d("ActivateTag", "ProfileRepository: ${e.message} ${e.cause}")
            Result.failure(NetworkError.ServerError)
        }
    }

    suspend fun addTag(
        serialNumber: String,
        verificationOrSerNumber: String,
        montenegrin: Boolean
    ): Result<Unit> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }
        val userToken = getUserToken() ?: return Result.failure(NetworkError.ServerError)

        return try {
            val lang = getLangKey()
            val response = if (montenegrin) {
                apiService(userToken).postAddTagME(
                    serialNumber = serialNumber,
                    serialNumberConfirmation = verificationOrSerNumber,
                    lang
                )
            } else {
                apiService(userToken).postAddTag(
                    serialNumber = serialNumber,
                    verificationCode = verificationOrSerNumber,
                    lang
                )
            }

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

    fun getCountryCode(): String? {
        return SharedPreferencesHelper.getCountryCode(context)
    }

    fun getIsFranchiser(): Boolean {
        return SharedPreferencesHelper.getIsFranchiser(context)
    }

    suspend fun getLocalBasicInfo(): BasicInfoEntity? {
        return database.basicInfoDao().getBasicInfo()
    }

}