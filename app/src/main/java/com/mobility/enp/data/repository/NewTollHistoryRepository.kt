package com.mobility.enp.data.repository

import android.content.Context
import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.mobility.enp.data.model.new_toll_history.local.entity.AllowedCountryEntity
import com.mobility.enp.data.model.new_toll_history.local.entity.SumTagEntity
import com.mobility.enp.data.model.new_toll_history.local.entity.TollHistoryItemEntity
import com.mobility.enp.data.paging.TollHistoryRemoteMediator
import com.mobility.enp.data.room.database.DRoom
import kotlinx.coroutines.flow.Flow

class NewTollHistoryRepository(
    database: DRoom,
    context: Context
) : BaseRepository(database, context){

    @OptIn(ExperimentalPagingApi::class)
    suspend fun getPagedHistory(
        filterCountry: String,
        dateFrom: String,
        dateTo: String,
        onUnauthorized: (httpCode: Int) -> Unit
    ): Flow<PagingData<TollHistoryItemEntity>> {
        val token = getUserToken().orEmpty()
        val lang = getLangKey()
        Log.d("MARKO", "getPagedHistory")
        Log.d(
            "MARKO",
            "getPagedHistory params country=$filterCountry dateFrom=$dateFrom dateTo=$dateTo"
        )
        return Pager(
            config = PagingConfig(
                pageSize = 15,
                enablePlaceholders = false,
                initialLoadSize = 15,
                prefetchDistance = 5
            ),
            remoteMediator = TollHistoryRemoteMediator(
                filterCountry = filterCountry,
                dateFrom = dateFrom,
                dateTo = dateTo,
                language = lang,
                apiService= apiService(token),
                database = database,
                onUnauthorized = onUnauthorized
            ),
            pagingSourceFactory = {
                database.newTollHistoryItemDao().pagingSource(filterCountry)
            }
        ).flow
    }

    fun observeAllowedCountries(): Flow<List<AllowedCountryEntity>> {
        return database.newAllowedCountryDao().observeAll()
    }

    fun isOnline(): Boolean {
        return isNetworkAvailable()
    }
}