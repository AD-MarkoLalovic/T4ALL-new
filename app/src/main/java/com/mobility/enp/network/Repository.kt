package com.mobility.enp.network

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.mobility.enp.R
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_home_page.HomePageFcmTokenResponse
import com.mobility.enp.data.model.api_home_page.homedata.HomeScreenData
import com.mobility.enp.data.model.api_my_invoices.BillDownload
import com.mobility.enp.data.model.api_my_invoices.BillsDetailsResponse
import com.mobility.enp.data.model.api_my_invoices.MyInvoicesResponse
import com.mobility.enp.data.model.api_my_profile.ChangePasswordRequest
import com.mobility.enp.data.model.api_my_profile.SupportRequest
import com.mobility.enp.data.model.api_my_profile.UpdateUserInfoRequest
import com.mobility.enp.data.model.api_my_profile.basic_information.BasicInformationResponse
import com.mobility.enp.data.model.cards.response.CardsResponse
import com.mobility.enp.data.model.api_room_models.FcmToken
import com.mobility.enp.data.model.api_tags.LostTagResponse
import com.mobility.enp.data.model.api_tags.PostLostTag
import com.mobility.enp.data.model.api_tags.TagsResponse
import com.mobility.enp.data.model.api_tool_history.listing.ToolHistoryListing
import com.mobility.enp.data.model.api_tool_history.complaint.ComplaintBody
import com.mobility.enp.data.model.api_tool_history.complaint.ObjectionBody
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import com.mobility.enp.data.model.countries.CountriesModel
import com.mobility.enp.data.model.csv_table.CsvModel
import com.mobility.enp.data.model.deactivation.DeactivateAccountModel
import com.mobility.enp.data.model.login.CustomerSupport
import com.mobility.enp.data.model.login.ForgotPasswordRequest
import com.mobility.enp.data.model.login.LoginBody
import com.mobility.enp.data.model.login.UserResponse
import com.mobility.enp.data.model.testmodels.UserList
import com.mobility.enp.data.room.api_related_daos.BasicInformationDao
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.view.adapters.my_invoices_adapters.BillsDetailsAdapter
import com.mobility.enp.view.adapters.my_invoices_adapters.MonthlyBillsAdapter
import com.mobility.enp.view.adapters.tool_history.main_screen.ToolHistoryListingAdapter
import com.mobility.enp.view.adapters.tool_history.result.HistoryResultAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

object Repository {

    const val TAG = "REPOSITORY"
    private const val PREFS_NAME = "user_prefs"
    private const val KEY_DISPLAY_NAME = "display_name"

    private fun apiService(token: String?): ApiService {
        return RestClient.create(ApiService::class.java, token).apiService
    }

    private fun apiServiceTest(): ApiService {
        return RestClient.create(ApiService::class.java, "").apiService
    }


    //endregion

    //region test
    fun fetchUserList(data: MutableLiveData<UserList>, context: Context, page: Int) {
        val call = apiServiceTest().getUserList(page)
        call.enqueue(object : Callback<UserList> {
            override fun onResponse(call: Call<UserList>, response: Response<UserList>) {
                if (response.isSuccessful) { // call is ok data is received ok
                    response.body()?.let {
                        data.postValue(it)
                    }

                } else { // call is 200 but parameters were incorrect for example it goes here
                    Toast.makeText(
                        context, "" + response.errorBody().toString(), Toast.LENGTH_SHORT
                    ).show()   // change this to dialog latter same for other error messages
                }
            }

            override fun onFailure(
                call: Call<UserList>, t: Throwable
            ) {   // <- api call fails codes 400 500 throwable gets the message
                Toast.makeText(context, t.message + "  " + t.cause, Toast.LENGTH_SHORT).show()
            }

        })

    }

    //endregion

    suspend fun loginUser(
        data: MutableLiveData<UserResponse>,
        context: Context,
        loginBody: LoginBody,
        errorBody: MutableLiveData<ErrorBody>
    ) {
        val lang = getUserLanguage(context)

        val call = apiService("").getUserLogin(lang, loginBody)
        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {


                if (response.isSuccessful) {
                    data.postValue(response.body())
                } else {
                    errorBody.postValue(getMessageFromErrorBody(response))
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.d(TAG, "Fcm Token onFailure: \n ${t.cause} \n\n ${t.message}")
            }

        })
    }

    //updated
    fun deleteFirebaseToken(auth: String, fcmToken: String, errorBody: MutableLiveData<ErrorBody>) {
        val call = apiService(auth).deleteFirebaseToken(fcmToken)
        call.enqueue(object : Callback<HomePageFcmTokenResponse> {
            override fun onResponse(
                call: Call<HomePageFcmTokenResponse>, response: Response<HomePageFcmTokenResponse>
            ) {
                if (response.isSuccessful) {
                    Log.d(TAG, "fcmToken is deleted : ${response.isSuccessful}")
                } else {
                    errorBody.postValue(getMessageFromErrorBody(response))
                }
            }

            override fun onFailure(call: Call<HomePageFcmTokenResponse>, t: Throwable) {
                Log.d(TAG, "Fcm token onFailure: \n ${t.cause} \n\n ${t.message}")
            }

        })
    }

    //updated
    suspend fun getUserHomeData(
        data: MutableLiveData<HomeScreenData>,
        token: String?,
        context: Context,
        errorBody: MutableLiveData<ErrorBody>
    ) {

        val lang = getUserLanguage(context)

        val call = apiService(token).getUserHomeData(lang)
        call.enqueue(object : Callback<HomeScreenData> {
            override fun onResponse(
                call: Call<HomeScreenData>, response: Response<HomeScreenData>
            ) {
                if (response.isSuccessful) {
                    data.postValue(response.body())
                } else {
                    errorBody.postValue(getMessageFromErrorBody(response))
                }
            }

            override fun onFailure(call: Call<HomeScreenData>, t: Throwable) {
                Log.d(TAG, "onFailure: \n ${t.cause} \n\n ${t.message}")
            }
        })
    }

    //updated
    fun postFcmToken(
        fcmToken: FcmToken, token: String?, errorBody: MutableLiveData<ErrorBody>
    ) {
        val call = apiService(token).postFirebaseFcmToken(fcmToken)
        call.enqueue(object : Callback<HomePageFcmTokenResponse> {
            override fun onResponse(
                call: Call<HomePageFcmTokenResponse>, response: Response<HomePageFcmTokenResponse>
            ) {
                if (response.isSuccessful) {
                    Log.d(TAG, "fcmToken posted is isSuccessful : ${response.isSuccessful}")
                } else {
                    errorBody.postValue(getMessageFromErrorBody(response))
                }
            }

            override fun onFailure(call: Call<HomePageFcmTokenResponse>, t: Throwable) {
                Log.d(TAG, "onFailure: \n ${t.cause} \n\n ${t.message}")
                val eb = ErrorBody(500, t.message + "\n" + t.cause)
                errorBody.postValue(eb)
            }
        })
    }

    // updated
    suspend fun getUserPersonalInfo(
        token: String?,
    ): BasicInformationResponse {
        return apiService(token).getUserPersonalData()
    }

    suspend fun updateUserInfo(
        basicInformationDao: BasicInformationDao, dataToSend: UpdateUserInfoRequest, token: String?
    ) {

        val response = apiService(token).updateUserInfo(dataToSend)

        if (response.isSuccessful) {
            val userInfo = response.body()?.data
            if (userInfo != null) {
                withContext(Dispatchers.IO) {
                    basicInformationDao.deleteBasicInfoData()
                    basicInformationDao.insertBasicInfoData(userInfo)
                }
            }
        } else {
            Log.d(TAG, "updateUserInfo: Error in response $response")
        }

    }

    fun logoutUser(
        token: String?, errorBody: MutableLiveData<ErrorBody>
    ) {
        val call = apiService(token).postLogoutUser()
        call.enqueue(object : Callback<HomePageFcmTokenResponse> {
            override fun onResponse(
                call: Call<HomePageFcmTokenResponse>, response: Response<HomePageFcmTokenResponse>
            ) {
                if (response.isSuccessful) {
                    Log.d(TAG, "user has been logged out : ${response.isSuccessful}")
                } else {
                    errorBody.postValue(getMessageFromErrorBody(response))
                }
            }

            override fun onFailure(call: Call<HomePageFcmTokenResponse>, t: Throwable) {
                Log.d(TAG, "onFailure: \n ${t.cause} \n\n ${t.message}")
            }

        })
    }

    //updated
    fun changePassword(
        request: ChangePasswordRequest,
        token: String?,
        context: Context,
        errorBody: MutableLiveData<ErrorBody>
    ) {
        val call = apiService(token).changePassword(request)
        call.enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Lozinka uspešno promenjena", Toast.LENGTH_SHORT).show()
                } else {
                    errorBody.postValue(getMessageFromErrorBody(response))
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Toast.makeText(
                    context, "Greška pri komunikaciji sa serverom: ${t.message}", Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    // skipped
    fun sendSupportMessage(
        request: SupportRequest, token: String?, errorBody: MutableLiveData<ErrorBody>
    ) {

        val call = apiService(token).sendContactMessage(request)
        call.enqueue(object : Callback<Unit> {

            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (!response.isSuccessful) {
                    errorBody.postValue(getMessageFromErrorBody(response))
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.d(TAG, "onFailure: \n ${t.cause} \n\n ${t.message}")
            }
        })
    }

    //updated
    fun getToolHistoryIndex(
        data: MutableLiveData<IndexData>, token: String, errorBody: MutableLiveData<ErrorBody>
    ) {
        val call = apiService(token).getToolHistoryIndex()
        call.enqueue(object : Callback<IndexData> {
            override fun onResponse(
                call: Call<IndexData>, response: Response<IndexData>
            ) {
                if (response.isSuccessful) {
                    data.postValue(response.body())
                } else {
                    errorBody.postValue(getMessageFromErrorBody(response))
                }
            }

            override fun onFailure(call: Call<IndexData>, t: Throwable) {
                Log.d(TAG, "onFailure: \n ${t.cause} \n\n ${t.message}")
            }
        })
    }

    //skipped its checked on history index same fragment
    suspend fun getToolHistoryListing(
        dataInterface: ToolHistoryListingAdapter.PassageDataInterface,
        token: String,
        tagSerialNumber: String,
        page: Int,
        perPage: Int,
        application: Context
    ) {

        val lang = getUserLanguage(application)

        val call = apiService(token).getToolHistoryTransit(
            tagSerialNumber, page.toString(), perPage.toString(), lang
        )
        call.enqueue(object : Callback<ToolHistoryListing> {
            override fun onResponse(
                call: Call<ToolHistoryListing>, response: Response<ToolHistoryListing>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { dataInterface.onOk(it) }
                } else {
                    dataInterface.onFailed(true, response.errorBody().toString())
                }
            }

            override fun onFailure(call: Call<ToolHistoryListing>, t: Throwable) {
                Log.d(TAG, "onFailure: \n ${t.cause} \n\n ${t.message}")
            }

        })
    }

    suspend fun getToolHistoryListingMutable(    // fills inner adapter data and sends errors to fragment if any
        data: MutableLiveData<ToolHistoryListing>,
        errorBody: MutableLiveData<ErrorBody>,
        token: String,
        tagSerialNumber: String,
        page: Int,
        perPage: Int,
        application: Context
    ) {

        val lang = getUserLanguage(application)

        val call = apiService(token).getToolHistoryTransit(
            tagSerialNumber, page.toString(), perPage.toString(), lang
        )
        call.enqueue(object : Callback<ToolHistoryListing> {
            override fun onResponse(
                call: Call<ToolHistoryListing>, response: Response<ToolHistoryListing>
            ) {
                if (response.isSuccessful) {
                    data.postValue(response.body())
                } else {
                    errorBody.postValue(getMessageFromErrorBody(response))
                }
            }

            override fun onFailure(call: Call<ToolHistoryListing>, t: Throwable) {
                Log.d(TAG, "onFailure: \n ${t.cause} \n\n ${t.message}")
            }

        })
    }

    suspend fun getToolHistoryListingMutableTimeFiltered(    // fills inner adapter data and sends errors to fragment if any
        data: MutableLiveData<ToolHistoryListing>,
        errorBody: MutableLiveData<ErrorBody>,
        token: String,
        tagSerialNumber: String,
        page: Int,
        perPage: Int,
        application: Context,
        filterFrom: String,
        filterTo: String,
        currency: String
    ) {

        val lang = getUserLanguage(application)

        val call = apiService(token).getToolHistoryTransitResultFragment(
            tagSerialNumber,
            page.toString(),
            perPage.toString(),
            filterFrom,
            filterTo,
            lang,
            currency
        )
        call.enqueue(object : Callback<ToolHistoryListing> {
            override fun onResponse(
                call: Call<ToolHistoryListing>, response: Response<ToolHistoryListing>
            ) {
                if (response.isSuccessful) {
                    data.postValue(response.body())
                } else {
                    errorBody.postValue(getMessageFromErrorBody(response))
                }
            }

            override fun onFailure(call: Call<ToolHistoryListing>, t: Throwable) {
                Log.d(TAG, "onFailure: \n ${t.cause} \n\n ${t.message}")
            }

        })
    }

    suspend fun getToolHistoryListingResult(
        dataInterface: HistoryResultAdapter.PassageDataInterface,
        token: String,
        tagSerialNumber: String,
        page: Int,
        perPage: Int,
        dateFrom: String,
        dateTo: String,
        application: Context,
        currency: String
    ) {

        val lang = getUserLanguage(application)

        val call = apiService(token).getToolHistoryTransitResultFragment(
            tagSerialNumber, page.toString(), perPage.toString(), dateFrom, dateTo, lang, currency
        )

        call.enqueue(object : Callback<ToolHistoryListing> {
            override fun onResponse(
                call: Call<ToolHistoryListing>, response: Response<ToolHistoryListing>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { dataInterface.onOk(it) }
                } else {
                    dataInterface.onFailed(true, response.errorBody().toString())
                }
            }

            override fun onFailure(call: Call<ToolHistoryListing>, t: Throwable) {
                Log.d(TAG, "onFailure: \n ${t.cause} \n\n ${t.message}")
            }
        })
    }

    fun deleteCard(
        cardId: String, token: String?, context: Context, errorBody: MutableLiveData<ErrorBody>
    ) {

        apiService(token).deleteCard(cardId).enqueue(object : Callback<Unit> {

            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.card_successfully_deleted),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    errorBody.postValue(getMessageFromErrorBody(response))
                    Toast.makeText(
                        context, context.getString(R.string.card_not_deleted), Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.d(TAG, "onFailure: \n ${t.cause} \n\n ${t.message}")
            }
        })
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

    suspend fun getTags(
        token: String?,
        page: Int,
        perPage: Int,
        errorBody: MutableLiveData<ErrorBody>,
        data: MutableLiveData<TagsResponse>,
        application: Context
    ) {

        val lang = getUserLanguage(application)

        val call = apiService(token).getUserTags(page.toString(), perPage.toString(), lang)
        call.enqueue(object : Callback<TagsResponse> {
            override fun onResponse(call: Call<TagsResponse>, response: Response<TagsResponse>) {
                if (response.isSuccessful) {
                    data.postValue(response.body())
                } else {
                    errorBody.postValue(getMessageFromErrorBody(response))
                }
            }

            override fun onFailure(call: Call<TagsResponse>, t: Throwable) {
                Log.d(TAG, "onFailure: \n ${t.cause} \n\n ${t.message}")
            }
        })

    }

    fun postLostTag(
        token: String?,
        body: PostLostTag,
        errorBody: MutableLiveData<ErrorBody>,
        mutableLiveData: MutableLiveData<LostTagResponse>
    ) {
        val call = apiService(token).postLostTag(body.serialNumber)
        call.enqueue(object : Callback<LostTagResponse> {
            override fun onResponse(
                call: Call<LostTagResponse>, response: Response<LostTagResponse>
            ) {
                if (response.isSuccessful) {
                    mutableLiveData.postValue(response.body())
                } else {
                    errorBody.postValue(getMessageFromErrorBody(response))
                }
            }

            override fun onFailure(call: Call<LostTagResponse>, t: Throwable) {
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


    fun postComplaint(
        token: String?,
        errorBody: MutableLiveData<ErrorBody>,
        complaintBody: ComplaintBody,
        data: MutableLiveData<LostTagResponse>
    ) {
        val call = apiService(token).postComplaint(complaintBody)
        call.enqueue(object : Callback<LostTagResponse> {
            override fun onResponse(
                call: Call<LostTagResponse>, response: Response<LostTagResponse>
            ) {
                if (response.isSuccessful) {
                    data.postValue(response.body())
                } else {
                    errorBody.postValue(getMessageFromErrorBody(response))
                }
            }

            override fun onFailure(call: Call<LostTagResponse>, t: Throwable) {
                Log.d(TAG, "onFailure: \n ${t.cause} \n\n ${t.message}")
            }

        })
    }

    fun postObjection(
        token: String?,
        errorBody: MutableLiveData<ErrorBody>,
        objectionBody: ObjectionBody,
        data: MutableLiveData<LostTagResponse>
    ) {
        val call = apiService(token).postObjection(objectionBody)
        call.enqueue(object : Callback<LostTagResponse> {
            override fun onResponse(
                call: Call<LostTagResponse>, response: Response<LostTagResponse>
            ) {
                if (response.isSuccessful) {
                    data.postValue(response.body())
                } else {
                    errorBody.postValue(getMessageFromErrorBody(response))
                }
            }

            override fun onFailure(call: Call<LostTagResponse>, t: Throwable) {
                Log.d(TAG, "onFailure: \n ${t.cause} \n\n ${t.message}")
            }

        })
    }

    suspend fun postAddTag(
        token: String?,
        serialNumber: String,
        verificationCode: String,
        errorBody: MutableLiveData<ErrorBody>,
        data: MutableLiveData<LostTagResponse>,
        application: Application
    ) {

        val lang = getUserLanguage(application)

        val call = apiService(token).postAddTag(verificationCode, serialNumber, lang)
        call.enqueue(object : Callback<LostTagResponse> {
            override fun onResponse(
                call: Call<LostTagResponse>, response: Response<LostTagResponse>
            ) {
                if (response.isSuccessful) {
                    data.postValue(response.body())
                } else {
                    errorBody.postValue(getMessageFromErrorBody(response))
                }
            }

            override fun onFailure(call: Call<LostTagResponse>, t: Throwable) {
                Log.d(TAG, "onFailure: \n ${t.cause} \n\n ${t.message}")
            }

        })
    }

    fun postPayBill(
        token: String?,
        billId: String?,
        context: Context,
        billPaid: MutableLiveData<Boolean>,
        errorBody: MutableLiveData<ErrorBody>
    ) {
        billId?.let {
            apiService(token).payBill(it).enqueue(object : Callback<Unit> {

                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {
                        billPaid.postValue(true)
                        Toast.makeText(
                            context,
                            context.getString(R.string.payment_successfully),
                            Toast.LENGTH_LONG
                        ).show()
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

    suspend fun setPrimaryCard(
        token: String?,
        billId: Int,
        errorBody: MutableLiveData<ErrorBody>,
        result: MutableLiveData<Boolean>,
        application: Application
    ) {

        val lang = getUserLanguage(application)

        try {
            val response = apiService(token).cardsSetDefault(billId, lang)
            if (response.isSuccessful) {
                result.postValue(true)
            } else {
                result.postValue(false)
                errorBody.postValue(getMessageFromErrorBody(response))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun postForgotPassword(
        email: ForgotPasswordRequest,
        errorBody: MutableLiveData<ErrorBody>,
        result: MutableLiveData<Boolean>
    ) {
        val call = apiService("").forgotPassword(email)

        call.enqueue(object : Callback<Unit> {

            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    result.postValue(true)
                } else {
                    result.postValue(false)
                    errorBody.postValue(getMessageFromErrorBody(response))
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.d(TAG, "onFailure: \n ${t.cause} \n\n ${t.message}")
                result.postValue(false)
            }

        })
    }

    suspend fun getCountryCode(token: String?): BasicInformationResponse {

        return apiService(token).getUserCountryCode()
    }

    suspend fun getUserCountries(
        data: MutableLiveData<CountriesModel>, errorBody: MutableLiveData<ErrorBody>, token: String
    ) {

        try {
            val response = apiService(token).getCountriesList()
            if (response.isSuccessful) {
                data.postValue(response.body())
            } else {
                errorBody.postValue(getMessageFromErrorBody(response))
            }
        } catch (e: Exception) {
            Log.d(TAG, "getUserCountries: ${e.cause} \n ${e.message}")
        }

    }

    suspend fun postFoundLostTag(
        token: String,
        serialNumber: String,
        errorBody: MutableLiveData<ErrorBody>,
        data: MutableLiveData<LostTagResponse>
    ) {
        try {
            val response = apiService(token).postFoundTag(serialNumber)
            if (response.isSuccessful) {
                data.postValue(response.body())
            } else {
                errorBody.postValue(getMessageFromErrorBody(response))
            }
        } catch (e: Exception) {
            Log.d(TAG, "getUserCountries: ${e.cause} \n ${e.message}")
        }
    }

    suspend fun sendCustomerSupport(
        customerSupport: CustomerSupport
    ): Response<Unit> {
        return apiService("").sendCustomerSupport(customerSupport)
    }

    suspend fun getCreditCards(
        data: MutableLiveData<CardsResponse>,
        token: String?,
        errorBody: MutableLiveData<ErrorBody>,
        application: Application
    ) {
        try {
            val lang = getUserLanguage(application)
            val response = apiService(token).getCreditCards(lang)
            if (response.isSuccessful) {
                data.postValue(response.body())
            } else {
                errorBody.postValue(getMessageFromErrorBody(response))
            }
        } catch (e: Exception) {
            Log.d(TAG, "getUserCards: ${e.cause} \n ${e.message}")
        }
    }

    suspend fun getCsvData(
        token: String?,
        application: Context,
        serialNumber: String,
        dateFrom: String,
        dateTo: String,
        currency: String,
        errorBody: MutableLiveData<ErrorBody>,
        data: MutableLiveData<CsvModel>
    ) {
        try {
            val lang = getUserLanguage(application)

            val response = apiService(token).getCsvData(
                serialNumber, lang, dateFrom, dateTo, currency
            )
            if (response.isSuccessful) {
                data.postValue(response.body())
            } else {
                errorBody.postValue(getMessageFromErrorBody(response))
            }
        } catch (e: HttpException) { // 500 400
            val errorResponse = e.response()?.errorBody()?.string() ?: "Server Error"
            errorBody.postValue(ErrorBody(500, errorResponse))
        }
    }


    suspend fun postDeactivateAccount(
        pair: Pair<String, String>,
        errorBody: MutableLiveData<ErrorBody>,
        data: MutableLiveData<DeactivateAccountModel>,
        token: String?,
        application: Application
    ) {
        try {
            val lang = getUserLanguage(application)
            val response =
                apiService(token).deactivateAccount(lang, pair.first, pair.second, "127.0.0.1")
            if (response.isSuccessful) {
                data.postValue(response.body())
            } else {
                errorBody.postValue(getMessageFromErrorBody(response))
            }
        } catch (e: HttpException) {
            Log.d(TAG, "getUserCards: ${e.cause} \n ${e.message}")
        }
    }

    suspend fun getUserLanguage(context: Context): String {  // cyr lat en de tr mk el
        val database: DRoom = DRoom.getRoomInstance(context)
        var lang = ""

        val languageTable = withContext(Dispatchers.IO) {
            database.languageDao().fetchAllowedUsers()
        }

        languageTable?.userLanguage?.let { languageCode ->
            if (languageCode.contains("sr")) {  // difference between country code for strings and parameter for query
                lang = "lat"
            } else if (languageCode.contains("cnr")) { // macedonian send me for language key cnr is for local strings
                lang = "me"
            } else if (languageCode.contains("el")) { // greek send gr
                lang = "gr"
            } else if (languageCode.contains("bs")) { // Bosnia
                lang = "ba"
            } else {
                lang = languageCode
            }
        }
        return lang
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

    private fun socketTimeOutMessage(t: Throwable, context: Context) {
        when (t) {
            is SocketTimeoutException -> {
                Toast.makeText(context, "SocketTimeOutError ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }

            is IOException -> {
                Log.d(TAG, "IOException: ${t.message} ${t.cause}")
            }

            else -> {
                Log.d(TAG, "Error: ${t.message} ${t.cause}")
            }
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun saveDisplayName(context: Context, displayName: String) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(KEY_DISPLAY_NAME, displayName).apply()
    }

    fun getDisplayName(context: Context): String? {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_DISPLAY_NAME, null)
    }

}