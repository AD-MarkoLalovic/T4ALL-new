package com.mobility.enp.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.mobility.enp.R
import com.mobility.enp.data.model.ApiErrorResponse
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.network.ApiService
import com.mobility.enp.network.RestClient
import com.mobility.enp.util.SharedPreferencesHelper
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
            database.loginDao().fetchAllowedUsers()?.accessToken ?:""
        }
    }

    protected fun getLangKey(): String {
        val languageCode = SharedPreferencesHelper.getUserLanguage(context)
        return when {
            languageCode.contains("sr") -> "lat"
            languageCode.contains("cnr") -> "me"
            languageCode.contains("el") -> "gr"
            languageCode.contains("bs") -> "ba"
            else -> languageCode
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
            .build()

        val jsonAdapter = moshi.adapter(ApiErrorResponse::class.java)

        val errorBodyString = try {
            errorBody.string()
        } catch (e: Exception) {
            return ApiErrorResponse(
                code = errorCode,
                message = context.getString(R.string.server_error_msg),
                errors = null
            )
        }

        return try {
            val parsedResponse = jsonAdapter.fromJson(errorBodyString)?.copy(code = errorCode)
                ?: ApiErrorResponse(
                    code = errorCode,
                    message = context.getString(R.string.server_error_msg),
                    errors = null
                )

            // Prerađujemo errors kako bi uvek bio lista stringova
            val processedErrors = when (parsedResponse.errors) {
                is Map<*, *> -> {  // Ako je objekat, uzimamo sve vrednosti kao listu stringova
                    (parsedResponse.errors).values.flatMap { it as? List<*> ?: emptyList() }
                        .mapNotNull { it.toString() }
                }

                is List<*> -> parsedResponse.errors // Ako je već lista, koristimo je
                else -> null  // Ako je nešto drugo, ignorišemo
            }

            // Vraćamo kopiju objekta sa obrađenim listama grešaka
            parsedResponse.copy(errors = processedErrors)

        } catch (e: Exception) {
            ApiErrorResponse(
                code = errorCode,
                message = context.getString(R.string.server_error_msg),
                errors = null
            )
        }
    }

}

