package com.mobility.enp.data.repository

import android.content.Context
import android.util.Log
import com.mobility.enp.data.model.api_tags.LostTagResponse
import com.mobility.enp.data.model.api_tool_history.complaint.ComplaintBody
import com.mobility.enp.data.model.api_tool_history.complaint.ObjectionBody
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2AllowedCountries
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponse
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponseCroatia
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponseCroatiaResult
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponseResult
import com.mobility.enp.data.model.cardsweb.CardWebModel
import com.mobility.enp.data.model.csv_table.CsvModel
import com.mobility.enp.data.model.pdf_table.CsvTable
import com.mobility.enp.data.model.pdf_table.FilterPdf
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.toCroatianPassage
import com.mobility.enp.util.toCroatianPassageResult
import com.mobility.enp.util.toV2HistoryTagResponseResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext


class PassageHistoryRepository(dRoom: DRoom, context: Context) : BaseRepository(dRoom, context) {

    companion object {
        const val TAG = "PASS_HISTORY"
    }

    suspend fun getTagBaseData(currentPage: Int, perPage: Int): Result<IndexData> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken() ?: return Result.failure(NetworkError.ServerError)

        return try {
            val lang = getLangKey()
            val response =
                apiService(userToken).getUserTagsNewForHistory(currentPage, perPage, lang)

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
            Log.d("MyTags", "ProfileRepository: ${e.message} ${e.cause}")
            Result.failure(NetworkError.ServerError)
        }
    }


    suspend fun getCardsFromServer(): Result<CardWebModel> {

        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()

        return try {
            val lang = getLangKey()
            val remoteData = apiService(userToken).getCreditCardsWeb(lang)
            if (remoteData.isSuccessful) {
                remoteData.body()?.let { responseBody ->
                    Result.success(responseBody)
                } ?: Result.failure(NetworkError.ServerError)
            } else {
                Result.failure(NetworkError.ServerError)
            }
        } catch (e: Exception) {
            Log.e("HomeRepository getCards", "Greška pri preuzimanju kartica: ${e.message}", e)
            Result.failure(NetworkError.ServerError)
        }
    }


    fun getAllowedCountriesFlow(): Flow<List<V2AllowedCountries>> {
        return database.historyV2AllowedCountriesDao().observeAllowedCountries()
    }

    suspend fun clearAllowedCountriesFlow() {
        database.historyV2AllowedCountriesDao().clear()
    }


    suspend fun deletePdfExportData() {
        database.pdfHistoryTableDao().deleteData()
    }

    suspend fun upsertPdfExportData(filterPdf: FilterPdf) {
        database.pdfHistoryTableDao().upsertData(filterPdf)
    }

    suspend fun upsertBaseTagData(indexData: IndexData) {
        database.toolHistoryDaoSerials().upsertData(indexData)
    }

    suspend fun roomUpsertV2Passages(data: V2HistoryTagResponse) {
        database.historyV2PassageDao().upsert(data)
    }

    suspend fun roomUpsertAllV2Passages(data: List<V2HistoryTagResponse>) {
        database.historyV2PassageDao().upsertAll(data)
    }

    suspend fun roomUpsertV2PassagesResult(data: V2HistoryTagResponse) {
        val passageData = data.toV2HistoryTagResponseResult()
        database.historyV2PassageDaoResult().upsert(passageData)
    }

    suspend fun roomUpsertAllV2PassagesResult(data: List<V2HistoryTagResponseResult>) {
        database.historyV2PassageDaoResult().upsertAll(data)
    }

    suspend fun roomUpsertAllIndexData(data: List<IndexData>) {
        database.toolHistoryDaoSerials().upsertDataAll(data)
    }

    suspend fun roomUpsertAllV2PassagesCroatia(data: List<V2HistoryTagResponseCroatia>) {
        database.historyPassageDaoV2Croatia().upsertAll(data)
    }

    suspend fun roomUpsertAllV2PassagesCroatiaResult(data: List<V2HistoryTagResponseCroatiaResult>) {
        database.historyPassageDaoV2CroatiaResult().upsertAll(data)
    }

    suspend fun roomUpsertAllowedCountries(data: List<String>) {
        val list: ArrayList<V2AllowedCountries> = arrayListOf()
        for (country in data) {
            list.add(V2AllowedCountries(country))
        }
        database.historyV2AllowedCountriesDao().upsert(list.toList())
    }

    suspend fun roomUpsertCroatianPassage(data: V2HistoryTagResponse) {
        val passageData = data.toCroatianPassage()
        database.historyPassageDaoV2Croatia().upsertData(passageData)
    }

    suspend fun roomUpsertCroatianPassageResult(data: V2HistoryTagResponse) {
        val passageData = data.toCroatianPassageResult()
        database.historyPassageDaoV2CroatiaResult().upsertData(passageData)
    }

    suspend fun fetchedStoredCsvData(): ByteArray? {
        return withContext(Dispatchers.IO) {
            database.csvTableDao().fetchData()?.data
        }
    }

    suspend fun fetchedStoredPDFData(): ByteArray? {
        return withContext(Dispatchers.IO) {
            database.pdfHistoryTableDao().fetchData()?.pdfData
        }
    }

    suspend fun getAdapterPassageData(
        tagSerialNumber: String,
        page: Int,
        perPage: Int, dateFrom: String, dateTo: String
    ): Result<V2HistoryTagResponse> {

        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()

        userToken?.let { token ->
            return try {
                val response = apiService(token).getToolHistoryTransitV2(
                    tagSerialNumber,
                    page.toString(),
                    perPage.toString(),
                    getLangKey(),
                    dateFrom,
                    dateTo
                )
                if (response.isSuccessful) {
                    response.body()?.let { indexData ->

                        val normalizedTags =
                            indexData.data?.tags?.sortedBy { it?.serialNumber } ?: emptyList()
                        indexData.data?.tags = normalizedTags

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


    suspend fun getAdapterPassageDataCountryFilter(
        tagSerialNumber: String,
        country: String,
        page: Int,
        perPage: Int, dateFrom: String, dateTo: String
    ): Result<V2HistoryTagResponse> {

        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()

        userToken?.let { token ->
            return try {
                Log.d(TAG, "getAdapterPassageDataCountryFilter: $dateFrom $dateTo")
                val response = apiService(token).getToolHistoryTransitV2Country(
                    tagSerialNumber,
                    country,
                    page.toString(),
                    perPage.toString(),
                    getLangKey(),
                    dateFrom,
                    dateTo
                )
                if (response.isSuccessful) {
                    response.body()?.let { indexData ->

                        val normalizedTags =
                            indexData.data?.tags?.sortedBy { it?.serialNumber } ?: emptyList()
                        indexData.data?.tags = normalizedTags

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
        country: String,

        ): Result<CsvModel> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }
        val userToken = getUserToken()

        userToken?.let {
            return try {
                val response = apiService(it).getCsvData(
                    tagSerial, getLangKey(), dateStartApi, dateEndApi, country
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

    suspend fun getPDFTableData(
        tagSerial: String,
        dateStartApi: String,
        dateEndApi: String,
        country: String,

        ): Result<ByteArray> {
        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }
        val userToken = getUserToken()

        userToken?.let {
            return try {
                val response = apiService(it).downloadPdf(
                    tagSerial, dateStartApi, dateEndApi, country, getLangKey()
                )

                if (response.isSuccessful) {
                    response.body()?.let { data ->
                        Result.success(data.bytes())
                    } ?: Result.failure(NetworkError.ServerError)
                } else {
                    response.errorBody()?.let { errorBody ->
                        val errorResponse = parseErrorResponse(response.code(), errorBody)
                        Result.failure(NetworkError.ApiError(errorResponse))
                    } ?: Result.failure(NetworkError.ServerError)
                }
            } catch (e: Exception) {
                Log.d(TAG, "data: ${e.message} ${e.cause}")
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

    fun isInternetAvailable(): Boolean {
        return isNetworkAvailable()
    }

}