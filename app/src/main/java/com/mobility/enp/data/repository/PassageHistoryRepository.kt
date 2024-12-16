package com.mobility.enp.data.repository

import android.content.Context
import android.util.Log
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.util.NetworkError


class PassageHistoryRepository(dRoom: DRoom, context: Context) : BaseRepository(dRoom, context) {

    companion object {
        const val TAG = "PASS_HISTORY"
    }

    suspend fun getIndexData(): Result<IndexData> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()
        userToken?.let { token ->
            return try {
                val response = apiService(token).getToolHistoryIndexN()
                if (response.isSuccessful) {
                    response.body()?.let { indexData ->
                        Result.success(indexData)
                    } ?: Result.failure(NetworkError.ServerError)
                } else {
                    response.errorBody()?.let { errorBody ->
                        val errorResponse = parseErrorResponse(errorBody)
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