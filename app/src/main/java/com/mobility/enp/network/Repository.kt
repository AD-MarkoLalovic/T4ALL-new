package com.mobility.enp.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_my_invoices.BillDownload
import com.mobility.enp.data.model.api_my_profile.basic_information.response.BasicInfoResponse
import com.mobility.enp.data.model.login.CustomerSupport
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