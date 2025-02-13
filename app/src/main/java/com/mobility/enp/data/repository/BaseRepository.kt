package com.mobility.enp.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.mobility.enp.R
import com.mobility.enp.data.model.ApiErrorResponse
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.network.ApiService
import com.mobility.enp.network.RestClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

// Bazni repo
abstract class BaseRepository(
    protected val database: DRoom,
    protected val context: Context
) {
    protected fun apiService(token: String?): ApiService {
        return RestClient.create(ApiService::class.java, token).apiService
    }

    protected suspend fun getUserToken(): String? {
        return withContext(Dispatchers.IO) {
            database.loginDao().fetchAllowedUsers().accessToken
        }
    }

    protected suspend fun getLangKey(): String {
        val languageTable = database.languageDao().fetchAllowedUsers()
        // Ako je userLanguage null, vraćamo en
        val languageCode = languageTable?.userLanguage ?: return "en"

        return when {
            languageCode.contains("sr") -> "lat"
            languageCode.contains("cnr") -> "me"
            languageCode.contains("el") -> "gr"
            languageCode.contains("bs") -> "ba"
            else -> languageCode
        }
    }

    suspend fun getRoomLanguage():String?{
        return withContext(Dispatchers.IO) {
            database.languageDao().fetchAllowedUsers()?.userLanguage
        }
    }

    protected fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    protected fun parseErrorResponse(errorCode: Int, errorBody: ResponseBody): ApiErrorResponse {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()  // Create Moshi instance for JSON parsing

        val jsonAdapter = moshi.adapter(ApiErrorResponse::class.java)

        // Read the error body string (avoid multiple reads)
        val errorBodyString = errorBody.string()

        // Try to parse the JSON and then set the error code; if parsing fails, return a default error object with the code.
        return jsonAdapter.fromJson(errorBodyString)?.copy(code = errorCode)
            ?: ApiErrorResponse(
                code = errorCode,
                message = context.getString(R.string.server_error_msg),
                errors = null
            )
    }
}

