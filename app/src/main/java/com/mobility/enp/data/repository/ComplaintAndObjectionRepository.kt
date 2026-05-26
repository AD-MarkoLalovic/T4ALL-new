package com.mobility.enp.data.repository

import android.content.Context
import android.util.Log
import com.mobility.enp.data.model.banks.entity.BanksEntity
import com.mobility.enp.data.model.new_toll_history.complaint.ComplaintBodyNew
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.util.NetworkError
import kotlinx.coroutines.flow.Flow

class ComplaintAndObjectionRepository(
    database: DRoom,
    context: Context
) : BaseRepository(database, context) {

    fun observeBanks(): Flow<List<BanksEntity>> = database.bankDao().observeAllBanks()

    suspend fun refreshBank() {
        if (!isNetworkAvailable()) return

        val token = getUserToken() ?: return

        try {
            val response = apiService(token).getBanks()
            if (response.isSuccessful) {
                val entities = response.body()
                    ?.data?.results?.toListBanksEntity()
                    ?: return
                database.bankDao().insertBanks(entities)
            }
        } catch (e: Exception) {
            Log.e("ComplaintAndObjectionRepository", "Neočekivana greška pri osvježavanju banaka", e)
        }
    }

    suspend fun postComplaint(body: ComplaintBodyNew): Result<Unit> {
        if (!isNetworkAvailable()) return Result.failure(NetworkError.NoConnection)

        val token = getUserToken() ?: return Result.failure(NetworkError.ServerError)

        return try {
            val response = apiService(token).postComplaintNew(body)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                response.errorBody()?.let { errorBody ->
                    val errorResponse = parseErrorResponse(response.code(), errorBody)
                    Result.failure(NetworkError.ApiError(errorResponse))
                } ?: Result.failure(NetworkError.ServerError)
            }
        } catch (e: Exception) {
            Log.e("ComplaintAndObjectionRepository", "Neočekivana greška pri podnosenju reklamacije", e)
            Result.failure(NetworkError.ServerError)
        }
    }
}