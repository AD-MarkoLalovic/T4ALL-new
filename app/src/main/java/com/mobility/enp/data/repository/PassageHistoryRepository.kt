package com.mobility.enp.data.repository

import android.content.Context
import android.util.Log
import com.mobility.enp.data.model.api_tags.LostTagResponse
import com.mobility.enp.data.model.api_tool_history.complaint.ComplaintBody
import com.mobility.enp.data.model.api_tool_history.complaint.ObjectionBody
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import com.mobility.enp.data.model.api_tool_history.listing.ToolHistoryListing
import com.mobility.enp.data.model.csv_table.CsvModel
import com.mobility.enp.data.model.pdf_table.CsvTable
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.util.NetworkError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


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
                        val errorResponse = parseErrorResponse(response.code(), errorBody)
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

    suspend fun getTagFill(
        tagSerialNumber: String,
        page: Int,
        perPage: Int,
    ): Result<ToolHistoryListing> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()

        userToken?.let { token ->
            return try {
                val response = apiService(token).getToolHistoryTransitNew(
                    tagSerialNumber, page.toString(), perPage.toString(), getLangKey()
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

        return Result.failure(NetworkError.ServerError)
    }

    suspend fun getToolHistoryTransitResult(
        tagSerialNumber: String,
        currentPage: String,
        itemPerPage: Int,
        dateFrom: String,
        dateTo: String,
        selectedCurrency: String
    ): Result<ToolHistoryListing> {

        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()

        userToken?.let {
            return try {
                val response = apiService(it).getToolHistoryTransitResultFragmentNew(
                    tagSerialNumber,
                    currentPage,
                    itemPerPage.toString(),
                    dateFrom,
                    dateTo,
                    getLangKey(),
                    selectedCurrency
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

        return Result.failure(NetworkError.ServerError)
    }

    //tagSerial,
    //                                dateStartApi,
    //                                dateEndApi,
    //                                selectedCurrency
    suspend fun getCsvTableData(
        tagSerial: String,
        dateStartApi: String,
        dateEndApi: String,
        selectedCurrency: String,

        ): Result<CsvModel> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }
        val userToken = getUserToken()

        userToken?.let {
            return try {
                val response = apiService(it).getCsvData(
                    tagSerial, getLangKey(), dateStartApi, dateEndApi, selectedCurrency
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

        return Result.failure(NetworkError.ServerError)
    }

    suspend fun getToolHistoryTransitResultPagination(
        tagSerialNumber: String,
        currentPage: String,
        itemPerPage: Int,
        dateFrom: String,
        dateTo: String,
        selectedCurrency: String
    ): Result<ToolHistoryListing> {

        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()

        userToken?.let {
            return try {
                val response = apiService(it).getToolHistoryTransitResultFragmentNew(
                    tagSerialNumber,
                    currentPage,
                    itemPerPage.toString(),
                    dateFrom,
                    dateTo,
                    getLangKey(),
                    selectedCurrency
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

        return Result.failure(NetworkError.ServerError)
    }

    suspend fun postComplaint(complaintBody: ComplaintBody): Result<LostTagResponse> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()
        userToken?.let { token ->
            return try {
                val response = apiService(token).postComplaintN(complaintBody)
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

        return Result.failure(NetworkError.ServerError)
    }


    suspend fun postObjection(objectionBody: ObjectionBody): Result<LostTagResponse> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()
        userToken?.let { token ->
            return try {
                val response = apiService(token).postObjectionN(objectionBody)
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

        return Result.failure(NetworkError.ServerError)
    }

    fun fetchContext(): Context {
        return context
    }

    suspend fun deleteCsvTable() {
        database.csvTableDao().deleteData()
    }

    suspend fun upsertCsvTable(csvTable: CsvTable) {
        database.csvTableDao().upsertData(csvTable)
    }

    suspend fun getIndexDataRoom(): IndexData {
        return withContext(Dispatchers.IO) {
            database.toolHistoryDao().fetchData()
        }
    }

    fun isInternetAvailable(): Boolean {
        return isNetworkAvailable()
    }

    suspend fun fetchPassageDataBySerial(serial: String): ToolHistoryListing? {
        return withContext(Dispatchers.IO) {
            database.toolListingDao().fetchPassageBySerial(serial)
        }
    }

}