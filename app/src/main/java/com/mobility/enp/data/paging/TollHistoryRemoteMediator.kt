package com.mobility.enp.data.paging

import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.mobility.enp.R
import com.mobility.enp.data.model.new_toll_history.local.entity.TollHistoryItemEntity
import com.mobility.enp.data.model.new_toll_history.local.entity.TollHistoryRemoteKeyEntity
import com.mobility.enp.data.model.new_toll_history.mapper.toTollHistoryEntities
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.network.ApiService
import com.mobility.enp.network.error.ApiMessageException
import com.mobility.enp.util.ApiErrorParser
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class TollHistoryRemoteMediator(
    context: Context,
    private val filterCountry: String,
    private val dateFrom: String,
    private val dateTo: String,
    private val language: String,
    private val apiService: ApiService,
    private val database: DRoom,
    private val onUnauthorized: (http: Int) -> Unit = {}
) : RemoteMediator<Int, TollHistoryItemEntity>() {

    private companion object {
        val UNAUTHORIZED_CODES = intArrayOf(401, 405)
    }

    private val queryKey = "$filterCountry|$dateFrom|$dateTo"
    private val appContext = context.applicationContext
    private val defaultErrorMessage = appContext.getString(R.string.server_error_msg)

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, TollHistoryItemEntity>
    ): MediatorResult {
        val page: Int = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> {
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            LoadType.APPEND -> {
                val remoteKey = database.newRemoteKeyDao().getByKey(queryKey)
                val next = remoteKey?.nextPage ?: return MediatorResult.Success(
                    endOfPaginationReached = true
                )
                next
            }
        }
        return try {
            val response = apiService.getNewTollHistory(
                country = filterCountry,
                page = page.toString(),
                perPage = state.config.pageSize.toString(),
                language = language,
                dataFrom = dateFrom,
                dateTo = dateTo
            )

            if (!response.isSuccessful) {
                val code = response.code()

                if (code in UNAUTHORIZED_CODES) {
                    onUnauthorized(code)
                    return MediatorResult.Success(endOfPaginationReached = true)
                }

                val parserError = response.errorBody()?.let { errorBody ->
                    ApiErrorParser.parseErrorResponse(
                        errorCode = code,
                        errorBody = errorBody,
                        defaultMessage = defaultErrorMessage
                    )
                }

                val message = parserError?.message
                    ?.takeIf { it.isNotBlank() }
                    ?: response.message().takeIf { it.isNotBlank() }
                    ?: defaultErrorMessage

                return MediatorResult.Error(
                    ApiMessageException(
                        code = code,
                        userMessage = message
                    )
                )
            }
            val body = response.body()
                ?: return MediatorResult.Error(IOException("Empty response body"))

            val lastPage = body.data?.records?.pagination?.lastPage ?: 1
            val endReached = page >= lastPage

            val startSortIndex = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.APPEND ->
                    database.newTollHistoryItemDao().maxSortIndexForCountry(filterCountry) + 1

                else -> 0
            }
            val entities  = body.toTollHistoryEntities(filterCountry, startSortIndex, page)

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.newTollHistoryItemDao().deleteByQuery(filterCountry)
                    database.newRemoteKeyDao().deleteByKey(queryKey)
                }

                database.newRemoteKeyDao().upsert(
                    TollHistoryRemoteKeyEntity(
                        queryKey = queryKey,
                        nextPage = if (endReached) null else page + 1
                    )
                )
                if (entities .isNotEmpty()) {
                    database.newTollHistoryItemDao().upsertAll(entities )
                }
            }
            MediatorResult.Success(endOfPaginationReached = endReached)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            val code = e.code()
            if (code in UNAUTHORIZED_CODES) {
                onUnauthorized(code)
                return MediatorResult.Success(endOfPaginationReached = true)
            }
            MediatorResult.Error(e)
        }
    }
}
