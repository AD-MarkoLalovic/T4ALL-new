package com.mobility.enp.data.repository

import android.content.Context
import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.mobility.enp.data.model.new_toll_history.local.entity.AllowedCountryEntity
import com.mobility.enp.data.model.new_toll_history.local.entity.TollHistoryItemEntity
import com.mobility.enp.data.model.new_toll_history.tags.remote.Tag
import com.mobility.enp.data.paging.TollHistoryRemoteMediator
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.util.NetworkError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NewTollHistoryRepository(
    database: DRoom,
    context: Context
) : BaseRepository(database, context){

    private val _customerCountry = MutableStateFlow<String?>(null)
    val customerCountry: StateFlow<String?> = _customerCountry.asStateFlow()

    @OptIn(ExperimentalPagingApi::class)
    suspend fun getPagedHistory(
        filterCountry: String,
        dateFrom: String,
        dateTo: String,
        onUnauthorized: (httpCode: Int) -> Unit
    ): Flow<PagingData<TollHistoryItemEntity>> {
        val token = getUserToken().orEmpty()
        val lang = getLangKey()

        return Pager(
            config = PagingConfig(
                pageSize = 15,
                enablePlaceholders = false,
                initialLoadSize = 15,
                prefetchDistance = 5
            ),
            remoteMediator = TollHistoryRemoteMediator(
                context = context.applicationContext,
                filterCountry = filterCountry,
                dateFrom = dateFrom,
                dateTo = dateTo,
                language = lang,
                apiService= apiService(token),
                database = database,
                onUnauthorized = onUnauthorized,
                onCustomerCountry = { code -> _customerCountry.value = code}
            ),
            pagingSourceFactory = {
                database.newTollHistoryItemDao().pagingSource(filterCountry)
            }
        ).flow
    }

    fun observeAllowedCountries(): Flow<List<AllowedCountryEntity>> {
        return database.newAllowedCountryDao().observeAll()
    }

    suspend fun fetchFilterTags(): Result<List<Tag>> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val token = getUserToken() ?: return Result.failure(NetworkError.ServerError)

        return try {
            val response = apiService(token).getBanksHistory()
            if (response.isSuccessful) {
                val tags = response.body()?.data?.tags?.filterNotNull().orEmpty()
                Result.success(tags)
            } else {
                response.errorBody()?.let { errorBody ->
                    val errorResponse = parseErrorResponse(response.code(), errorBody)
                    Result.failure(NetworkError.ApiError(errorResponse))
                } ?: Result.failure(NetworkError.ServerError)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching toll history filter tags", e)
            Result.failure(NetworkError.ServerError)
        }
    }

    companion object {
        private const val TAG = "NewTollHistoryRepository"
    }
}