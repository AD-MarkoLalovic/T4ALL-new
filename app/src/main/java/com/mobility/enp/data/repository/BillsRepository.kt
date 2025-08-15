package com.mobility.enp.data.repository

import android.content.Context
import android.util.Log
import com.mobility.enp.data.model.api_my_invoices.BillDownload
import com.mobility.enp.data.model.api_my_invoices.BillsDetailsResponse
import com.mobility.enp.data.model.api_my_invoices.refactor.MyInvoicesResponse
import com.mobility.enp.data.model.pdf_table.PdfTable
import com.mobility.enp.data.repository.PassageHistoryRepository.Companion.TAG
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.util.NetworkError

class BillsRepository(dRoom: DRoom, context: Context) : BaseRepository(dRoom, context) {


    suspend fun getTokenTemp(): String? {
        val userToken = getUserToken()
        return userToken
    }

    fun isNetworkPresent(): Boolean {
        return isNetworkAvailable()
    }

    suspend fun setLocalBillsData(bills: MyInvoicesResponse) {
        database.myInvoicesDao().deleteDataMonthlyInvoices()
        database.myInvoicesDao().insertMonthlyInvoices(bills)
    }

    suspend fun fetchSavedBillsData(): MyInvoicesResponse {
        return database.myInvoicesDao().fetchDataMonthlyInvoices()
    }

    suspend fun savePdfData(decodedData: ByteArray) {
        database.pdfDao().deleteData()
        database.pdfDao().upsertData(PdfTable(0, decodedData))
    }

    suspend fun getPdfTable(): PdfTable {
        return database.pdfDao().fetchData()
    }


    suspend fun getInvoicesData(
        perPage: Int,
        selectedCountry: String
    ): Result<MyInvoicesResponse> {

        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()

        userToken?.let { token ->
            return try {
                val response =
                    apiService(token).getInvoicesPerMonth(getLangKey(), 1, perPage, selectedCountry)
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

    suspend fun downloadPdfData(
        billId: String,
    ): Result<BillDownload> {

        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()

        userToken?.let { token ->
            return try {
                val response =
                    apiService(token).getPdfBill(billId)
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


    suspend fun downloadPassageData(
        billId: String,
    ): Result<BillDownload> {

        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()

        userToken?.let { token ->
            return try {
                val response =
                    apiService(token).getPdfListingPasses(billId)
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

    suspend fun getInvoicesDataPaging(
        page: Int,
        perPage: Int,
        selectedCountry: String
    ): Result<MyInvoicesResponse> {

        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()

        userToken?.let { token ->
            return try {
                val response =
                    apiService(token).getInvoicesPerMonth(
                        getLangKey(),
                        page,
                        perPage,
                        selectedCountry
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


    suspend fun getBillDetails(
        yearMonth: String,
        currency: String,
        perPage: Int,
        selectedCountry: String
    ): Result<BillsDetailsResponse> {

        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()

        userToken?.let { token ->
            return try {
                val response =
                    apiService(token).getInvoicesMonthlyDetails(
                        yearMonth,
                        currency,
                        1,
                        perPage,
                        getLangKey(),
                        selectedCountry
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


    suspend fun getBillDetailsPaging(
        yearMonth: String,
        currency: String,
        perPage: Int,
        selectedCountry: String, page: Int
    ): Result<BillsDetailsResponse> {

        if (!isNetworkAvailable()) {
            return Result.failure(NetworkError.NoConnection)
        }

        val userToken = getUserToken()

        userToken?.let { token ->
            return try {
                val response =
                    apiService(token).getInvoicesMonthlyDetails(
                        yearMonth,
                        currency,
                        page,
                        perPage,
                        getLangKey(),
                        selectedCountry
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

}