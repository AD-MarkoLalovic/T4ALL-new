package com.mobility.enp.network

import com.mobility.enp.data.model.api_home_page.HomePageFcmTokenResponse
import com.mobility.enp.data.model.api_home_page.homedata.HomeScreenData
import com.mobility.enp.data.model.api_my_invoices.BillDownload
import com.mobility.enp.data.model.api_my_invoices.BillsDetailsResponse
import com.mobility.enp.data.model.api_my_invoices.MyInvoicesResponse
import com.mobility.enp.data.model.api_my_profile.ChangePasswordRequest
import com.mobility.enp.data.model.api_my_profile.SupportRequest
import com.mobility.enp.data.model.api_my_profile.UpdateUserInfoRequest
import com.mobility.enp.data.model.api_my_profile.basic_information.BasicInformationResponse
import com.mobility.enp.data.model.api_my_profile.basic_information.entity.BasicInfoEntity
import com.mobility.enp.data.model.api_my_profile.basic_information.request.UpdateUserDataRequest
import com.mobility.enp.data.model.api_my_profile.basic_information.response.BasicInfoResponse
import com.mobility.enp.data.model.api_my_profile.refund_request.SendRefundRequest
import com.mobility.enp.data.model.api_my_profile.refund_request.response.RefundRequestsResponse
import com.mobility.enp.data.model.api_my_profile.refund_request.tags.response.TagsResponseRefundRequest
import com.mobility.enp.data.model.api_room_models.FcmToken
import com.mobility.enp.data.model.api_tags.LostTagResponse
import com.mobility.enp.data.model.api_tags.TagsResponse
import com.mobility.enp.data.model.api_tool_history.listing.ToolHistoryListing
import com.mobility.enp.data.model.api_tool_history.complaint.ComplaintBody
import com.mobility.enp.data.model.api_tool_history.complaint.ObjectionBody
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import com.mobility.enp.data.model.banks.response.BanksResponse
import com.mobility.enp.data.model.cards.response.CardsResponse
import com.mobility.enp.data.model.countries.CountriesModel
import com.mobility.enp.data.model.csv_table.CsvModel
import com.mobility.enp.data.model.deactivation.DeactivateAccountModel
import com.mobility.enp.data.model.login.CustomerSupport
import com.mobility.enp.data.model.login.ForgotPasswordRequest
import com.mobility.enp.data.model.login.LoginBody
import com.mobility.enp.data.model.login.UserResponse
import com.mobility.enp.data.model.testmodels.UserList
import com.mobility.enp.view.ui_models.BasicInfoUIModel
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    //region test calls
    @GET("/api/users")
    fun getUserList(
        @Query("page") param: Int
    ): Call<UserList>

    //endregion

    //region new api call

    @POST("/api/v1/login")
    fun getUserLogin(
        @Query("lang") language: String,
        @Body user: LoginBody
    ): Call<UserResponse>

    @POST("/api/v1/logout")
    fun postLogoutUser(
    ): Call<HomePageFcmTokenResponse>

    @GET("/api/v1/home")
    fun getUserHomeData(
        @Query(value = "lang") language: String
    ): Call<HomeScreenData>

    @POST("/api/v1/firebase")
    fun postFirebaseFcmToken(
        @Body fcmToken: FcmToken
    ): Call<HomePageFcmTokenResponse>

    @DELETE("/api/v1/firebase/{fcm_token}")
    fun deleteFirebaseToken(
        @Path(value = "fcm_token") token: String
    ): Call<HomePageFcmTokenResponse>

    @GET("/api/v1/personal-data")
    suspend fun getUserPersonalData(): BasicInformationResponse

    @GET("/api/v1/personal-data")
    suspend fun getUserData(): Response<BasicInfoResponse>

    @GET("/api/v1/personal-data")
    suspend fun getUserCountryCode(): BasicInformationResponse

    @PUT("/api/v1/personal-data")
    suspend fun updateUserInformation(@Body request: UpdateUserInfoRequest): Response<BasicInformationResponse>

    @PUT("/api/v1/personal-data")
    suspend fun updateUserInfo(
        @Body request: UpdateUserDataRequest,
        @Query("lang") language: String
    ): Response<BasicInfoResponse>

    @PUT("/api/v1/personal-data/change-password")
    fun changePassword(@Body request: ChangePasswordRequest): Call<Unit>

    @POST("/api/v1/contact")
    fun sendContactMessage(@Body request: SupportRequest): Call<Unit>

    @GET("/api/v1/history/tags")
    fun getToolHistoryIndex(): Call<IndexData>

    @GET("/api/v1/history/tags")
    suspend fun getToolHistoryIndexN(): Response<IndexData>

    @DELETE("/api/v1/cards/{card_id}")
    fun deleteCard(
        @Path("card_id") cardId: String
    ): Call<Unit>

    @GET("/api/v1/history/transit")
    fun getToolHistoryTransit(
        @Query("filter[serial_numbers]") serialNumbers: String,  // can be multiple but then send them as 18150144618,18150144612 string
        @Query("page") page: String, // current page
        @Query("perPage") perPage: String, // items per page
        @Query("lang") language: String
    ): Call<ToolHistoryListing>

    @GET("/api/v1/history/transit")
    suspend fun getToolHistoryTransitNew(
        @Query("filter[serial_numbers]") serialNumbers: String,  // can be multiple but then send them as 18150144618,18150144612 string
        @Query("page") page: String, // current page
        @Query("perPage") perPage: String, // items per page
        @Query("lang") language: String
    ): Response<ToolHistoryListing>

    @GET("/api/v1/history/transit")
    fun getToolHistoryTransitResultFragment(
        @Query("filter[serial_numbers]") serialNumbers: String,  // can be multiple but then send them as 18150144618,18150144612 string
        @Query("page") page: String, // current page
        @Query("perPage") perPage: String, // items per page
        @Query("filter[date_from]") dateFrom: String,  // format to send dd.MM.yyyy
        @Query("filter[date_to]") dateTo: String,
        @Query("lang") language: String,
        @Query("filter[currency]") currency: String
    ): Call<ToolHistoryListing>

    @GET("/api/v1/history/transit")
    suspend fun getToolHistoryTransitResultFragmentNew(
        @Query("filter[serial_numbers]") serialNumbers: String,  // can be multiple but then send them as 18150144618,18150144612 string
        @Query("page") page: String, // current page
        @Query("perPage") perPage: String, // items per page
        @Query("filter[date_from]") dateFrom: String,  // format to send dd.MM.yyyy
        @Query("filter[date_to]") dateTo: String,
        @Query("lang") language: String,
        @Query("filter[currency]") currency: String
    ): Response<ToolHistoryListing>

    @GET("/api/v1/bills")
    fun getInvoicesIndex(
        @Query(value = "lang") language: String,
        @Query("perPage") perPage: Int // items per page
    ): Call<MyInvoicesResponse>

    @GET("/api/v1/bills")
    fun getInvoicesIndexPaging(
        @Query(value = "lang") language: String,
        @Query("page") page: Int, // current page
        @Query("perPage") perPage: Int // items per page
    ): Call<MyInvoicesResponse>

    @GET("/api/v1/bills/month")
    fun getBillsByMonth(
        @Query("filter[yearMonth]") yearMonth: String,
        @Query("filter[currency]") currency: String,
        //@Query("page") page: Int, // current page
        @Query("perPage") perPage: Int,
        @Query(value = "lang") language: String

    ): Call<BillsDetailsResponse>

    @GET("/api/v1/bills/month")
    fun getBillsByMonthPaging(
        @Query("filter[yearMonth]") yearMonth: String,
        @Query("filter[currency]") currency: String,
        @Query("page") page: Int, // current page
        @Query("perPage") perPage: Int,
        @Query(value = "lang") language: String
    ): Call<BillsDetailsResponse>

    @GET("/api/v1/tags")
    fun getUserTags(
        @Query("page") page: String,
        @Query("perPage") perPage: String,
        @Query(value = "lang") language: String
    ): Call<TagsResponse>

    //endregion

    @GET("/api/v1/tags")
    suspend fun getTagsRefundRequest(
        @Query("perPage") perPage: Int
    ): Response<TagsResponseRefundRequest>

    @FormUrlEncoded
    @POST("/api/v1/tags/lost-tag")
    fun postLostTag(
        @Field("serialNumber") serialNumber: String
    ): Call<LostTagResponse>

    @FormUrlEncoded
    @POST("/api/v1/tags/add-tag")
    fun postAddTag(
        @Field("verificationCode") verificationCode: String,
        @Field("serialNumber") serialNumber: String,
        @Query(value = "lang") languageKey: String
    ): Call<LostTagResponse>

    @POST("/api/v1/history/complaint")
    fun postComplaint(
        @Body complaintBody: ComplaintBody
    ): Call<LostTagResponse>

    @POST("/api/v1/history/objection")
    fun postObjection(
        @Body objectionBody: ObjectionBody
    ): Call<LostTagResponse>

    @POST("/api/v1/history/complaint")
    suspend fun postComplaintN(
        @Body complaintBody: ComplaintBody
    ): Response<LostTagResponse>

    @POST("/api/v1/history/objection")
    suspend fun postObjectionN(
        @Body objectionBody: ObjectionBody
    ): Response<LostTagResponse>

    @GET("/api/v1/bills/invoice/{bill_id}/bill/pdf")
    fun getPdfBill(
        @Path(value = "bill_id") billId: String
    ): Call<BillDownload>

    @GET("/api/v1/bills/invoice/{bill_id}/list/passes/export/pdf")
    suspend fun getPdfListingPasses(
        @Path(value = "bill_id") billId: String
    ): Response<BillDownload>

    @POST("/api/v1/bills/pay/{bill_id}/bill")
    fun payBill(
        @Path(value = "bill_id") billId: String
    ): Call<Unit>

    @GET("/api/v1/refund-requests")
    suspend fun refundRequest(
        @Query("lang") language: String
    ): Response<RefundRequestsResponse>

    @POST("/api/v1/refund-requests")
    suspend fun sendRefundRequest(
        @Query("lang") language: String,
        @Body request: SendRefundRequest
    ): Response<Unit>

    @POST("/api/v1/cards/default/{bill_id}")
    suspend fun cardsSetDefault(
        @Path(value = "bill_id") billId: Int,
        @Query(value = "lang") language: String
    ): Response<Unit>

    @POST("/api/v1/forgot-password")
    fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Call<Unit>

    @GET("/api/v1/countries")
    suspend fun getCountriesList(): Response<CountriesModel>

    @FormUrlEncoded
    @POST("/api/v1/tags/found-tag")
    suspend fun postFoundTag(
        @Field("serialNumber") serialNumber: String
    ): Response<LostTagResponse>

    @GET("/api/v1/cards")
    suspend fun getCreditCards(@Query("lang") language: String): Response<CardsResponse>

    @FormUrlEncoded
    @POST("/api/v1/delete-account-request")
    suspend fun deactivateAccount(
        @Query("lang") language: String,
        @Field("email") email: String,
        @Field("message") message: String,
        @Field("ip_address") ip_address: String
    ): Response<DeactivateAccountModel>

    @POST("/api/v1/support")
    suspend fun sendCustomerSupport(
        @Body customerSupport: CustomerSupport
    ): Response<Unit>

    @GET("/api/v1/history/export")
    suspend fun getCsvData(
        @Query(value = "serial_number") serialNumber: String,
        @Query(value = "locale") locale: String,
        @Query(value = "date_from") dateFrom: String,
        @Query(value = "date_to") dateTo: String,
        @Query(value = "currency") currency: String
    ): Response<CsvModel>


    @GET("/api/v1/banks")
    suspend fun getBanks(): Response<BanksResponse>

    @PUT("api/v1/personal-data/change-language")
    suspend fun changeLanguage(@Query("language") languageCode: String)
}