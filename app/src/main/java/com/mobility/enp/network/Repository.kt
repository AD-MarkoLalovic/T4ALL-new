package com.mobility.enp.network

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_my_invoices.BillDownload
import com.mobility.enp.data.model.api_my_invoices.BillsDetailsResponse
import com.mobility.enp.data.model.api_my_invoices.MyInvoicesResponse
import com.mobility.enp.data.model.api_my_profile.basic_information.response.BasicInfoResponse
import com.mobility.enp.data.model.login.CustomerSupport
import com.mobility.enp.util.SharedPreferencesHelper
import com.mobility.enp.view.adapters.my_invoices_adapters.BillsDetailsAdapter
import com.mobility.enp.view.adapters.my_invoices_adapters.MonthlyBillsAdapter
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object Repository {

    const val TAG = "REPOSITORY"

    private fun apiService(token: String?): ApiService {
        return RestClient.create(ApiService::class.java, token).apiService
    }

    // updated
    suspend fun getUserPersonalInfo(
        token: String?
    ): BasicInfoResponse {
        return apiService(token).getUserPersonalData()
    }

    suspend fun getInvoices(
        data: MutableLiveData<MyInvoicesResponse>,
        token: String?,
        errorBody: MutableLiveData<ErrorBody>,
        application: Application,
        perPage: Int
    ) {

        val lang = getUserLanguage(application)

        apiService(token).getInvoicesIndex(lang, perPage)
            .enqueue(object : Callback<MyInvoicesResponse> {

                override fun onResponse(
                    call: Call<MyInvoicesResponse>, response: Response<MyInvoicesResponse>
                ) {
                    if (response.isSuccessful) {
                        data.postValue(response.body())
                    } else {
                        errorBody.postValue(getMessageFromErrorBody(response))
                    }
                }

                override fun onFailure(call: Call<MyInvoicesResponse>, t: Throwable) {
                    Log.d(TAG, "onFailure: \n ${t.cause} \n\n ${t.message}")
                }

            })

    }

    suspend fun getInvoicesPaging(
        data: MutableLiveData<MyInvoicesResponse>,
        token: String?,
        errorBody: MutableLiveData<ErrorBody>,
        application: Application,
        page: Int,
        perPage: Int
    ) {

        val lang = getUserLanguage(application)

        apiService(token).getInvoicesIndexPaging(lang, page, perPage)
            .enqueue(object : Callback<MyInvoicesResponse> {

                override fun onResponse(
                    call: Call<MyInvoicesResponse>, response: Response<MyInvoicesResponse>
                ) {
                    if (response.isSuccessful) {
                        data.postValue(response.body())
                    } else {
                        errorBody.postValue(getMessageFromErrorBody(response))
                    }
                }

                override fun onFailure(call: Call<MyInvoicesResponse>, t: Throwable) {
                    Log.d(TAG, "onFailure: \n ${t.cause} \n\n ${t.message}")
                }

            })

    }

    suspend fun getBillsDetails(
        data: MonthlyBillsAdapter.FetchBillsDetails,
        token: String?,
        yearMonth: String,
        currency: String,
        perPage: Int,
        errorBody: MutableLiveData<ErrorBody>, application: Context
    ) {

        val lang = getUserLanguage(application)

        apiService(token).getBillsByMonth(yearMonth, currency, perPage, lang)
            .enqueue(object : Callback<BillsDetailsResponse> {

                override fun onResponse(
                    call: Call<BillsDetailsResponse>, response: Response<BillsDetailsResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            data.onOK(it)
                        }
                    } else {
                        errorBody.postValue(getMessageFromErrorBody(response))
                        data.onFailed()
                    }
                }

                override fun onFailure(call: Call<BillsDetailsResponse>, t: Throwable) {
                    Log.d(TAG, "onFailure: \n ${t.cause} \n\n ${t.message}")
                }
            })
    }

    suspend fun getBillsDetailsPaging(
        data: MutableLiveData<BillsDetailsResponse>,
        token: String?,
        yearMonth: String,
        currency: String,
        page: Int,
        perPage: Int,
        errorBody: MutableLiveData<ErrorBody>, application: Context
    ) {

        val lang = getUserLanguage(application)

        apiService(token).getBillsByMonthPaging(yearMonth, currency, page, perPage, lang)
            .enqueue(object : Callback<BillsDetailsResponse> {

                override fun onResponse(
                    call: Call<BillsDetailsResponse>, response: Response<BillsDetailsResponse>
                ) {
                    if (response.isSuccessful) {
                        data.postValue(response.body())
                    } else {
                        errorBody.postValue(getMessageFromErrorBody(response))
                    }
                }

                override fun onFailure(call: Call<BillsDetailsResponse>, t: Throwable) {
                    Log.d(TAG, "onFailure: \n ${t.cause} \n\n ${t.message}")
                }
            })
    }

    fun getBillDetailsPdf(
        data: BillsDetailsAdapter.DownloadBillsDetails,
        token: String?,
        billId: String,
        errorBody: MutableLiveData<ErrorBody>
    ) {
        apiService(token).getPdfBill(billId).enqueue(object : Callback<BillDownload> {

            override fun onResponse(call: Call<BillDownload>, response: Response<BillDownload>) {
                if (response.isSuccessful) {
                    response.body().let {
                        data.onOK(it)
                    }
                } else {
                    data.onFailed()
                    errorBody.postValue(getMessageFromErrorBody(response))
                }
            }

            override fun onFailure(call: Call<BillDownload>, t: Throwable) {
                Log.d(TAG, "onFailure: \n ${t.cause} \n\n ${t.message}")
            }

        })
    }

    suspend fun getListingPasses(
        token: String, billId: String
    ): Response<BillDownload> {

        return apiService(token).getPdfListingPasses(billId)
    }

    fun postPayBill(
        token: String?,
        billId: String?,
        billPaid: MutableLiveData<Boolean>,
        errorBody: MutableLiveData<ErrorBody>
    ) {
        billId?.let {
            apiService(token).payBill(it).enqueue(object : Callback<Unit> {

                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {
                        billPaid.postValue(true)
                    } else {
                        billPaid.postValue(false)
                        errorBody.postValue(getMessageFromErrorBody(response))
                    }

                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Log.d(TAG, "onFailure: \n ${t.cause} \n\n ${t.message}")
                }
            })
        }
    }

    suspend fun sendCustomerSupport(
        customerSupport: CustomerSupport
    ): Response<Unit> {
        return apiService("").sendCustomerSupport(customerSupport)
    }

    fun getUserLanguage(context: Context): String {

        val languageCode = SharedPreferencesHelper.getUserLanguage(context)
        return when {
            languageCode.contains("sr") -> "lat"
            languageCode.contains("cnr") -> "me"
            languageCode.contains("el") -> "gr"
            languageCode.contains("bs") -> "ba"
            else -> languageCode
        }
    }

    private fun <T> getMessageFromErrorBody(response: Response<T>): ErrorBody {
        val error = ErrorBody(response.code(), "")

        if (response.errorBody() != null) {
            try {
                val jsonObject = JSONObject(response.errorBody()!!.string())
                error.errorBody = (jsonObject.getString("message"))
            } catch (e: Exception) {
                try {
                    if (response.errorBody()!!.string().isEmpty()) {
                        error.errorBody = ("Server error occurred. Please try again later.")
                    } else {
                        error.errorBody = (response.errorBody()!!.string())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return error
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }


}