package com.mobility.enp.viewmodel

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.mobility.enp.MyApplication
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tags.LostTagResponse
import com.mobility.enp.data.model.api_tool_history.TimeSave
import com.mobility.enp.data.model.api_tool_history.complaint.ComplaintBody
import com.mobility.enp.data.model.api_tool_history.complaint.ObjectionBody
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import com.mobility.enp.data.model.api_tool_history.index.Tag
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponse
import com.mobility.enp.data.model.cardsweb.CardWebModel
import com.mobility.enp.data.model.csv_table.CsvModel
import com.mobility.enp.data.model.franchise.FranchiseModel
import com.mobility.enp.data.model.pdf_table.CsvTable
import com.mobility.enp.data.repository.PassageHistoryRepository
import com.mobility.enp.services.MyFirebaseMessagingService.Companion.CHANNEL_ID
import com.mobility.enp.services.MyFirebaseMessagingService.Companion.NOTIFICATION_ID
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SharedPreferencesHelper
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.view.CsvActivity
import com.mobility.enp.view.adapters.tool_history.main_and_filter_screen.ToolHistoryListingAdapter
import com.mobility.enp.view.fragments.tool_history.ToolHistoryFilterFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserPassViewModel(private val repository: PassageHistoryRepository) : ViewModel() {

    companion object {
        const val TAG = "PassViewModel"
        const val TOKEN = "API_TOKEN"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepository = (this[APPLICATION_KEY] as MyApplication).passageHistoryRepository
                UserPassViewModel(
                    repository = myRepository
                )
            }
        }
    }

    private val _baseTagDataState =
        MutableStateFlow<SubmitResult<Pair<IndexData, CardWebModel?>>>(SubmitResult.Loading)
    val baseTagDataState: StateFlow<SubmitResult<Pair<IndexData, CardWebModel?>>> get() = _baseTagDataState

    private val _baseTagDataStateFilterFragment =
        MutableStateFlow<SubmitResult<Pair<IndexData, CardWebModel?>>>(SubmitResult.Loading)
    val baseTagDataStateFilterFragment: StateFlow<SubmitResult<Pair<IndexData, CardWebModel?>>> get() = _baseTagDataStateFilterFragment


    private val _filterListFilter = MutableStateFlow<List<String>>(emptyList())
    val filterList: StateFlow<List<String>> = _filterListFilter.asStateFlow()

    fun setFilterList(newItems: List<String>) {
        _filterListFilter.value = newItems
    }

    private val _filterListTagData = MutableStateFlow<IndexData?>(null)
    val filterTagData: StateFlow<IndexData?> = _filterListTagData.asStateFlow()

    fun setFilterTagData(indexData: IndexData) {
        _filterListTagData.value = indexData
    }

    private val _baseTagDataStateByCountry =
        MutableStateFlow<SubmitResult<IndexData>>(SubmitResult.Loading)
    val baseTagDataStateByCountry: StateFlow<SubmitResult<IndexData>> get() = _baseTagDataStateByCountry

    private val _csvTable = MutableStateFlow<SubmitResult<CsvModel>>(SubmitResult.Empty)
    val csvTable: StateFlow<SubmitResult<CsvModel>> get() = _csvTable

    fun setCsvState() {
        _csvTable.value = SubmitResult.Empty
    }

    private val _complaintObjectionState =
        MutableStateFlow<SubmitResult<LostTagResponse>>(SubmitResult.Empty)
    val complaintObjectionState: StateFlow<SubmitResult<LostTagResponse>> get() = _complaintObjectionState

    private val _complaintObjectionStateFiltered =
        MutableStateFlow<SubmitResult<LostTagResponse>>(SubmitResult.Empty)
    val complaintObjectionStateFiltered: StateFlow<SubmitResult<LostTagResponse>> get() = _complaintObjectionStateFiltered

    fun setStateIndex(indexData: IndexData) { // from room
        _baseTagDataState.value =
            SubmitResult.Empty  // needs to be set to empty before using saved data by country filter or it will only work once
        _baseTagDataState.value = SubmitResult.Success(Pair(indexData, CardWebModel(null, null)))
    }

    var startDate = MutableLiveData<TimeSave>()
    var endDate = MutableLiveData<TimeSave>()
    private var userSelectedCalendarStart: Long? = null
    private var userSelectedCalendarEnd: Long? = null


    var allTagsSelected = false

    var selectedCountry: String = ""

    fun nullData() {
        startDate.value = TimeSave(null, null)
        endDate.value = TimeSave(null, null)
        userSelectedCalendarStart = null
        userSelectedCalendarEnd = null
        indexData = null
        selectedCountry = ""
    }

    private val itemsPerPage = 10

    fun isNetAvailable(): Boolean {
        return repository.isInternetAvailable()
    }

    fun getBaseDataAlternativeApi() {   // uses faster api call to get serial numbers of tags saving about 10 seconds on server response time
        _baseTagDataState.value = SubmitResult.Loading
        viewModelScope.launch(Dispatchers.IO) {

            val resultTags = async {
                repository.getTagBaseData(1, 5)
            }

            val resultCards = async {
                repository.getCardsFromServer()
            }

            val tagResultDeferred = resultTags.await()
            val resultCardsDeferred = resultCards.await()

            if (tagResultDeferred.isSuccess && resultCardsDeferred.isSuccess) {

                val tagsData = tagResultDeferred.getOrNull()
                val cardData = resultCardsDeferred.getOrNull()

                if (tagsData == null || cardData == null) {
                    _baseTagDataState.value = SubmitResult.Empty
                } else {
                    _baseTagDataState.value = SubmitResult.Success(Pair(tagsData, cardData))
                }

            } else {
                when (val error = tagResultDeferred.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.e(
                            "UserPassVM",
                            "Error while fetching tags data",
                            error
                        )
                        _baseTagDataState.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _baseTagDataState.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(
                                    "API_TOKEN UserPassViewModel",
                                    "invalid token detected login out user"
                                )
                                _baseTagDataState.value =
                                    SubmitResult.InvalidApiToken(
                                        error.errorResponse.code,
                                        error.errorResponse.message ?: ""
                                    )
                            }

                            else -> {
                                _baseTagDataState.value =
                                    SubmitResult.FailureApiError(
                                        error.errorResponse.message ?: ""
                                    )
                                Log.d(
                                    TAG,
                                    "UserPassViewModel api error ${error.errorResponse.message}"
                                )
                            }
                        }
                    }
                }

                when (val error = resultCardsDeferred.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.e(
                            "UserPassVM",
                            "Error while fetching cards data",
                            error
                        )
                        _baseTagDataState.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _baseTagDataState.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(
                                    "API_TOKEN UserPassViewModel",
                                    "invalid token detected login out user"
                                )
                                _baseTagDataState.value =
                                    SubmitResult.InvalidApiToken(
                                        error.errorResponse.code,
                                        error.errorResponse.message ?: ""
                                    )
                            }

                            else -> {
                                _baseTagDataState.value =
                                    SubmitResult.FailureApiError(
                                        error.errorResponse.message ?: ""
                                    )
                                Log.d(
                                    TAG,
                                    "UserPassViewModel api error ${error.errorResponse.message}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun getBaseDataAlternativeApiFilterFragment() {   // uses faster api call to get serial numbers of tags saving about 10 seconds on server response time
        _baseTagDataStateFilterFragment.value = SubmitResult.Loading
        viewModelScope.launch(Dispatchers.IO) {

            val resultTags = async {
                repository.getTagBaseData(1, 5)
            }

            val resultCards = async {
                repository.getCardsFromServer()
            }

            val tagResultDeferred = resultTags.await()
            val resultCardsDeferred = resultCards.await()

            if (tagResultDeferred.isSuccess && resultCardsDeferred.isSuccess) {

                val tagsData = tagResultDeferred.getOrNull()
                val cardData = resultCardsDeferred.getOrNull()

                if (tagsData == null || cardData == null) {
                    _baseTagDataStateFilterFragment.value = SubmitResult.Empty
                } else {
                    _baseTagDataStateFilterFragment.value =
                        SubmitResult.Success(Pair(tagsData, cardData))
                }

            } else {
                when (val error = tagResultDeferred.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.e(
                            "UserPassVM",
                            "Error while fetching tags data",
                            error
                        )
                        _baseTagDataStateFilterFragment.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _baseTagDataStateFilterFragment.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(
                                    "API_TOKEN UserPassViewModel",
                                    "invalid token detected login out user"
                                )
                                _baseTagDataStateFilterFragment.value =
                                    SubmitResult.InvalidApiToken(
                                        error.errorResponse.code,
                                        error.errorResponse.message ?: ""
                                    )
                            }

                            else -> {
                                _baseTagDataStateFilterFragment.value =
                                    SubmitResult.FailureApiError(
                                        error.errorResponse.message ?: ""
                                    )
                                Log.d(
                                    TAG,
                                    "UserPassViewModel api error ${error.errorResponse.message}"
                                )
                            }
                        }
                    }
                }

                when (val error = resultCardsDeferred.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.e(
                            "UserPassVM",
                            "Error while fetching cards data",
                            error
                        )
                        _baseTagDataStateFilterFragment.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _baseTagDataStateFilterFragment.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(
                                    "API_TOKEN UserPassViewModel",
                                    "invalid token detected login out user"
                                )
                                _baseTagDataStateFilterFragment.value =
                                    SubmitResult.InvalidApiToken(
                                        error.errorResponse.code,
                                        error.errorResponse.message ?: ""
                                    )
                            }

                            else -> {
                                _baseTagDataStateFilterFragment.value =
                                    SubmitResult.FailureApiError(
                                        error.errorResponse.message ?: ""
                                    )
                                Log.d(
                                    TAG,
                                    "UserPassViewModel api error ${error.errorResponse.message}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    fun getBaseDataAlternativeApiForCountriesOnMain() {   // uses faster api call to get serial numbers of tags saving about 10 seconds on server response time
        _baseTagDataStateByCountry.value = SubmitResult.Loading
        viewModelScope.launch(Dispatchers.IO) {

            val resultTags = async {
                repository.getTagBaseData(1, 5)
            }

            val tagResultDeferred = resultTags.await()

            if (tagResultDeferred.isSuccess) {

                val tagsData = tagResultDeferred.getOrNull()

                if (tagsData == null) {
                    _baseTagDataStateByCountry.value = SubmitResult.Empty
                } else {
                    _baseTagDataStateByCountry.value = SubmitResult.Success(tagsData)
                }

            } else {
                when (val error = tagResultDeferred.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.e(
                            "UserPassVM",
                            "Error while fetching tags data",
                            error
                        )
                        _baseTagDataStateByCountry.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _baseTagDataStateByCountry.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(
                                    "API_TOKEN UserPassViewModel",
                                    "invalid token detected login out user"
                                )
                                _baseTagDataStateByCountry.value =
                                    SubmitResult.InvalidApiToken(
                                        error.errorResponse.code,
                                        error.errorResponse.message ?: ""
                                    )
                            }

                            else -> {
                                _baseTagDataStateByCountry.value =
                                    SubmitResult.FailureApiError(
                                        error.errorResponse.message ?: ""
                                    )
                                Log.d(
                                    TAG,
                                    "UserPassViewModel api error ${error.errorResponse.message}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun getBaseTagDataPagination(
        nextPage: Int,
        perPage: Int,
        flow: MutableStateFlow<SubmitResult<IndexData>>
    ) {
        viewModelScope.launch(Dispatchers.IO) {  // 2 flows success returns to adapter and issues return to fragment
            val result = repository.getTagBaseData(nextPage, perPage)
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data == null) {
                    flow.value = SubmitResult.Empty
                } else {
                    flow.value = SubmitResult.Success(data)
                }
            } else {
                when (val error = result.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.d(TAG, "Error while fetching tag serial data")
                        _baseTagDataState.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _baseTagDataState.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(
                                    "API_TOKEN UserPassViewModel",
                                    "invalid token detected login out user"
                                )
                                _baseTagDataState.value =
                                    SubmitResult.InvalidApiToken(
                                        error.errorResponse.code,
                                        error.errorResponse.message ?: ""
                                    )
                            }

                            else -> {
                                _baseTagDataState.value =
                                    SubmitResult.FailureApiError(
                                        error.errorResponse.message ?: ""
                                    )
                                Log.d(
                                    "UserPassViewModel",
                                    "UserPassViewModel api error ${error.errorResponse.message}"
                                )
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    fun postComplaint(complaintBody: ComplaintBody) {
        _complaintObjectionState.value = SubmitResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.postComplaint(complaintBody)
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data == null) {
                    _complaintObjectionState.value = SubmitResult.Empty
                } else {
                    _complaintObjectionState.value = SubmitResult.Success(data)
                }
            } else {
                when (val error = result.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.d(TAG, "Error while fetching tag serial data")
                        _baseTagDataState.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _baseTagDataState.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(
                                    "API_TOKEN UserPassViewModel",
                                    "invalid token detected login out user"
                                )
                                _baseTagDataState.value =
                                    SubmitResult.InvalidApiToken(
                                        error.errorResponse.code,
                                        error.errorResponse.message ?: ""
                                    )
                            }

                            else -> {
                                _baseTagDataState.value =
                                    SubmitResult.FailureApiError(
                                        error.errorResponse.message ?: ""
                                    )
                                Log.d(
                                    "UserPassViewModel",
                                    "UserPassViewModel api error ${error.errorResponse.message}"
                                )
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }


    fun postComplaintFiltered(complaintBody: ComplaintBody) {
        _complaintObjectionStateFiltered.value = SubmitResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.postComplaint(complaintBody)
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data == null) {
                    _complaintObjectionStateFiltered.value = SubmitResult.Empty
                } else {
                    _complaintObjectionStateFiltered.value = SubmitResult.Success(data)
                }
            } else {
                when (val error = result.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.d(TAG, "Error while fetching tag serial data")
                        _complaintObjectionStateFiltered.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _complaintObjectionStateFiltered.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(
                                    "API_TOKEN UserPassViewModel",
                                    "invalid token detected login out user"
                                )
                                _complaintObjectionStateFiltered.value =
                                    SubmitResult.InvalidApiToken(
                                        error.errorResponse.code,
                                        error.errorResponse.message ?: ""
                                    )
                            }

                            else -> {
                                _complaintObjectionStateFiltered.value =
                                    SubmitResult.FailureApiError(
                                        error.errorResponse.message ?: ""
                                    )
                                Log.d(
                                    "UserPassViewModel",
                                    "UserPassViewModel api error ${error.errorResponse.message}"
                                )
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }


    fun postObjectionFiltered(objectionBody: ObjectionBody) {
        _complaintObjectionStateFiltered.value = SubmitResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.postObjection(objectionBody)
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data == null) {
                    _complaintObjectionStateFiltered.value = SubmitResult.Empty
                } else {
                    _complaintObjectionStateFiltered.value = SubmitResult.Success(data)
                }
            } else {
                when (val error = result.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.d(TAG, "Error while fetching tag serial data")
                        _complaintObjectionStateFiltered.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _complaintObjectionStateFiltered.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(
                                    "API_TOKEN UserPassViewModel",
                                    "invalid token detected login out user"
                                )
                                _complaintObjectionStateFiltered.value =
                                    SubmitResult.InvalidApiToken(
                                        error.errorResponse.code,
                                        error.errorResponse.message ?: ""
                                    )
                            }

                            else -> {
                                _complaintObjectionStateFiltered.value =
                                    SubmitResult.FailureApiError(
                                        error.errorResponse.message ?: ""
                                    )
                                Log.d(
                                    "UserPassViewModel",
                                    "UserPassViewModel api error ${error.errorResponse.message}"
                                )
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    fun postObjection(objectionBody: ObjectionBody) {
        _complaintObjectionState.value = SubmitResult.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.postObjection(objectionBody)
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data == null) {
                    _complaintObjectionState.value = SubmitResult.Empty
                } else {
                    _complaintObjectionState.value = SubmitResult.Success(data)
                }
            } else {
                when (val error = result.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.d(TAG, "Error while fetching tag serial data")
                        _baseTagDataState.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _baseTagDataState.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(
                                    "API_TOKEN UserPassViewModel",
                                    "invalid token detected login out user"
                                )
                                _baseTagDataState.value =
                                    SubmitResult.InvalidApiToken(
                                        error.errorResponse.code,
                                        error.errorResponse.message ?: ""
                                    )
                            }

                            else -> {
                                _baseTagDataState.value =
                                    SubmitResult.FailureApiError(error.errorResponse.message ?: "")
                                Log.d(TAG, "api error ${error.errorResponse.message}")
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * this function fills the initial 10 passages in tool history once we have tag serial data
     */
    fun getToolHistoryTransit(
        flow: MutableStateFlow<SubmitResult<V2HistoryTagResponse>>,
        tagSerialNumber: String,
        currentPage: Int
    ) {
        flow.value = SubmitResult.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val dateFrom = startDate.value?.formattedTime
            val dateTo = endDate.value?.formattedTime

            var result =
                repository.getAdapterPassageData(
                    tagSerialNumber,
                    currentPage,
                    itemsPerPage,
                    dateFrom ?: "",
                    dateTo ?: ""
                )

            if (!selectedCountry.isEmpty()) {
                result = repository.getAdapterPassageDataCountryFilter(
                    tagSerialNumber,
                    selectedCountry,
                    currentPage,
                    itemsPerPage, dateFrom ?: "", dateTo ?: ""
                )
            }
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data == null) {
                    flow.value = SubmitResult.Empty
                } else {
                    flow.value = SubmitResult.Success(data)
                }
            } else {
                when (val error = result.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.d(TAG, "Error while fetching tag serial data")
                        _baseTagDataState.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _baseTagDataState.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(TOKEN, "invalid token detected login out user")
                                _baseTagDataState.value =
                                    SubmitResult.InvalidApiToken(
                                        error.errorResponse.code ?: 0,
                                        error.errorResponse.message ?: ""
                                    )
                            }

                            else -> {
                                _baseTagDataState.value =
                                    SubmitResult.FailureApiError(
                                        error.errorResponse.message ?: ""
                                    )
                                Log.d(TAG, "api error ${error.errorResponse.message}")
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }


    fun getToolHistoryTransitResultFragment(
        flow: MutableStateFlow<SubmitResult<V2HistoryTagResponse>>,
        tagSerialNumber: String,
        currentPage: Int
    ) {
        flow.value = SubmitResult.Loading

        viewModelScope.launch(Dispatchers.IO) {

            val dateFrom = startDate.value?.formattedTime
            val dateTo = endDate.value?.formattedTime

            val result = repository.getAdapterPassageDataCountryFilter(
                tagSerialNumber,
                selectedCountry,
                currentPage,
                itemsPerPage,
                dateFrom ?: "",
                dateTo ?: ""
            )
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data == null) {
                    flow.value = SubmitResult.Empty
                } else {
                    flow.value = SubmitResult.Success(data)
                }
            } else {
                when (val error = result.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.d(TAG, "Error while fetching tag serial data")
                        _baseTagDataState.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _baseTagDataState.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(TOKEN, "invalid token detected login out user")
                                _baseTagDataState.value =
                                    SubmitResult.InvalidApiToken(
                                        error.errorResponse.code ?: 0,
                                        error.errorResponse.message ?: ""
                                    )
                            }

                            else -> {
                                _baseTagDataState.value =
                                    SubmitResult.FailureApiError(
                                        error.errorResponse.message ?: ""
                                    )
                                Log.d(TAG, "api error ${error.errorResponse.message}")
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    fun fetchStoredData(
        dataInterface: ToolHistoryListingAdapter.PassageDataInterface,
        tagSerialNumber: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.fetchPassageDataBySerialNew(tagSerialNumber, selectedCountry)?.let {
                withContext(Dispatchers.Main) {
                    dataInterface.onOk(it)
                }
            }
        }
    }

    private fun postNotification() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Tool4all", NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "Tool4all"
        channel.enableLights(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        channel.lightColor = Color.BLUE

        val intent = Intent(repository.fetchContext(), CsvActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(
                repository.fetchContext(),
                100,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

        val notificationManager =
            repository.fetchContext().getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(
            repository.fetchContext(), CHANNEL_ID
        ).setSmallIcon(R.drawable.splash_logo)
            .setContentTitle(repository.fetchContext().getString(R.string.export))
            .setContentIntent(pendingIntent)
            .setContentText(repository.fetchContext().getString(R.string.csv_saved))
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(
                repository.fetchContext(), Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(repository.fetchContext())
            .notify(NOTIFICATION_ID, builder.build())

    }

    fun processCsvData(csvModel: CsvModel, nameExtra: String, context: Context) {
        Log.d(TAG, "csv data: $csvModel")

        if (!csvModel.data?.csvContent.isNullOrEmpty()) {
            csvModel.data?.csvContent?.let { data ->
                viewModelScope.launch(Dispatchers.IO) {
                    saveCsvLocally(data, nameExtra, context) // <- csv excel export
                }
            }
        }
    }

    fun deletePassageData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRoomData()
        }
    }

    private suspend fun saveCsvLocally(encoded: String, nameExtra: String, context: Context) =
        coroutineScope {
            try {
                // Decode the Base64 string
                val decodedBytes = Base64.decode(encoded, Base64.DEFAULT)
                val decodedString = String(decodedBytes)

                // Parse the data and format it as CSV
                val rows = decodedString.split("\n")
                val csvBuilder = StringBuilder()

                // Add a header if your data format is known

                val billNumber = ContextCompat.getString(context, R.string.bill_number)
                val price = ContextCompat.getString(context, R.string.price)
                val payRamp = ContextCompat.getString(context, R.string.pay_ramp)
                val timeOfPassage = ContextCompat.getString(context, R.string.time_of_passage)

                val titleHeader =
                    StringBuilder().append(billNumber).append(",").append(timeOfPassage).append(",")
                        .append(payRamp).append(",").append(price).append(",").append("\n")

                csvBuilder.append(titleHeader.toString())

                // Add each row to the CSV content
                for (row in rows) {
                    csvBuilder.append(row).append("\n")
                }

                // Save CSV to shared storage using MediaStore
                val fileName = "export-$nameExtra.csv"
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH, "Documents/"
                    ) // Save in the Documents folder
                }

                // Get the URI for the file in shared storage
                val uri = repository.fetchContext().contentResolver.insert(
                    MediaStore.Files.getContentUri("external"), contentValues
                )

                uri?.let { fileUri ->
                    repository.fetchContext().contentResolver.openOutputStream(fileUri)
                        ?.use { outputStream ->
                            // Write the CSV content to the file
                            outputStream.write(csvBuilder.toString().toByteArray())
                            outputStream.flush()
                            Log.d(
                                ToolHistoryFilterFragment.TAG,
                                "CSV file saved successfully in Documents folder."
                            )
                        } ?: Log.d(ToolHistoryFilterFragment.TAG, "Failed to open OutputStream.")
                } ?: Log.d(
                    ToolHistoryFilterFragment.TAG,
                    "Failed to create file URI in MediaStore."
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    fun saveBase64ToCSV(base64Data: String, nameExtra: String, context: Context) {
        viewModelScope.launch {
            try {

                // Regular expression to match CSV fields with commas, allowing quoted fields to contain commas
                val regex = """"([^"]*)"|([^",]+)""".toRegex()

                // Decode the Base64 string
                val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                val decodedString = String(decodedBytes)

                val rows = decodedString.split("\n")

                val billNumber = ContextCompat.getString(context, R.string.bill_number)
                val price = ContextCompat.getString(context, R.string.price)
                val payRamp = ContextCompat.getString(context, R.string.pay_ramp)
                val timeOfPassage = ContextCompat.getString(context, R.string.time_of_passage)

                val headers = listOf(
                    billNumber,
                    timeOfPassage,
                    payRamp,
                    price
                )

                val byteArrayOutputStream = ByteArrayOutputStream()
                val pdfWriter = PdfWriter(byteArrayOutputStream)
                val pdfDocument = PdfDocument(pdfWriter)
                val document = Document(pdfDocument)

                val boldFont =
                    PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD)

                val table = Table(headers.size)

                headers.forEach { header ->
                    table.addCell(Cell().add(Paragraph(header).setFont(boldFont)))
                }


                rows.forEach { row ->
                    val columns = mutableListOf<String>()
                    val matches = regex.findAll(row)
                    matches.forEach { match ->
                        // Get either quoted field or unquoted field
                        columns.add(match.groupValues[1].ifEmpty { match.groupValues[2] })
                    }

                    columns.forEach { column ->
                        table.addCell(Cell().add(Paragraph(column)))
                    }
                }

                document.add(table)
                document.close()

                val pdfData = byteArrayOutputStream.toByteArray()
                repository.deleteCsvTable()
                repository.upsertCsvTable(CsvTable(0, pdfData))

                // Save CSV to shared storage using MediaStore
                val fileName = "export-$nameExtra.csv"
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH, "Documents/"
                    ) // Save in the Documents folder
                }

                val uri = repository.fetchContext().contentResolver.insert(
                    MediaStore.Files.getContentUri("external"), contentValues
                )

                uri?.let { fileUri ->
                    repository.fetchContext().contentResolver.openOutputStream(fileUri)
                        ?.use { outputStream ->
                            outputStream.write(pdfData)
                            outputStream.flush()

                            postNotification()

                            Log.d(
                                ToolHistoryFilterFragment.TAG,
                                "PDF file saved successfully in Documents folder."
                            )
                        } ?: Log.d(ToolHistoryFilterFragment.TAG, "Failed to open OutputStream.")
                } ?: Log.d(
                    ToolHistoryFilterFragment.TAG,
                    "Failed to create file URI in MediaStore."
                )

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    suspend fun fetchIndexData(): IndexData? {
        return withContext(Dispatchers.IO) {
            repository.getIndexDataRoom()
        }
    }

    fun deleteTagSerialData() {
        _baseTagDataState.value = SubmitResult.Empty
        _baseTagDataStateByCountry.value = SubmitResult.Empty
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTagSerialData()
        }
    }

    private var _data: MutableLiveData<IndexData> = MutableLiveData<IndexData>()
    val data: LiveData<IndexData> get() = _data

    var selectedTags: ArrayList<Tag> = ArrayList()
    var indexData: IndexData? = null
    var tagForExport: Tag? = null
    var listOfCountries: List<String> = emptyList()

    suspend fun insertRoomToolHistoryIndexData(indexData: IndexData) {
        repository.insertRoomTagBaseData(indexData)
    }

    suspend fun insertPassageData(toolHistoryListing: V2HistoryTagResponse) {
        toolHistoryListing.countryCode = selectedCountry
        repository.insertPassageDataAdapter(toolHistoryListing)
    }

    fun showDatePicker(fromDate: Boolean, context: Context, franchiseModel: FranchiseModel?) {
        viewModelScope.launch {
            val selectedDate: Long = if (fromDate) {
                if (userSelectedCalendarStart != null) {
                    userSelectedCalendarStart!!
                } else {
                    System.currentTimeMillis()
                }
            } else {
                if (userSelectedCalendarEnd != null) {
                    userSelectedCalendarEnd!!
                } else {
                    System.currentTimeMillis()
                }
            }

            Log.d(TAG, "showDatePicker: ${convertLongToDateString(selectedDate)}")

            val locale = when (val lang = SharedPreferencesHelper.getUserLanguage(context)) {
                "cyr" -> Locale("sr", "RS")
                "sr", "cnr" -> Locale("sr_Latn", "RS", "Latn")
                else -> Locale(lang)
            }

            Locale.setDefault(locale)
            val config = context.resources.configuration
            config.setLocale(locale)
            context.createConfigurationContext(config)

            val constraintsBuilder = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())


            franchiseModel?.let { model ->
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(context.getString(R.string.select_date))
                    .setSelection(selectedDate)
                    .setCalendarConstraints(constraintsBuilder.build())
                    .setNegativeButtonText(context.getString(R.string.cancel))
                    .setPositiveButtonText(context.getString(R.string.confirm))
                    .setTheme(model.franchiseCalendarStyle)
                    .build()

                datePicker.addOnPositiveButtonClickListener {// time in long
                    try {
                        val date: TimeSave = convertLongToDateString(it)

                        if (fromDate) {
                            userSelectedCalendarStart = it
                            startDate.postValue(date)
                        } else {
                            userSelectedCalendarEnd = it
                            endDate.postValue(date)
                        }

                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.please_enter_date_manually),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                val fm = (context as AppCompatActivity).supportFragmentManager
                datePicker.show(fm, "dateSelect")
            } ?: run {
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(context.getString(R.string.select_date))
                    .setSelection(selectedDate)
                    .setCalendarConstraints(constraintsBuilder.build())
                    .setNegativeButtonText(context.getString(R.string.cancel))
                    .setPositiveButtonText(context.getString(R.string.confirm))
                    .build()

                datePicker.addOnPositiveButtonClickListener {// time in long
                    try {
                        val date: TimeSave = convertLongToDateString(it)

                        if (fromDate) {
                            userSelectedCalendarStart = it
                            startDate.postValue(date)
                        } else {
                            userSelectedCalendarEnd = it
                            endDate.postValue(date)
                        }

                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.please_enter_date_manually),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                val fm = (context as AppCompatActivity).supportFragmentManager
                datePicker.show(fm, "dateSelect")
            }
        }
    }

    private fun convertLongToDateString(time: Long): TimeSave {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        val date = Date(time)
        val formDate = sdf.format(date)
        return TimeSave(formDate, date)
    }

    fun getCsvData(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            if (startDate.value?.inDateForm?.time != null && endDate.value?.inDateForm?.time != null) {
                if (endDate.value?.inDateForm!!.before(startDate.value?.inDateForm!!)) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.end_date_check),
                        Toast.LENGTH_SHORT
                    ).show()
                    _csvTable.value = SubmitResult.Empty
                } else {
                    try {
                        _csvTable.value = SubmitResult.Loading
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

                        val dateStart = Date(userSelectedCalendarStart ?: 0)
                        val dateEnd = Date(userSelectedCalendarEnd ?: 0)

                        val dateStartApi = sdf.format(dateStart)
                        val dateEndApi = sdf.format(dateEnd)

                        Log.d(TAG, "startDate: $dateStartApi endDate $dateEndApi")

                        if (selectedTags.isNotEmpty() && selectedTags.size == 1 || allTagsSelected) {

                            val tagSerial = if (allTagsSelected) {
                                ""
                            } else {
                                tagForExport?.serialNumber
                                    ?: ""  // if one item last selected tag is added
                            }

                            val result = repository.getCsvTableData(
                                tagSerial,
                                dateStartApi,
                                dateEndApi,
                                selectedCountry
                            )

                            val body = result.getOrNull()
                            body?.let { data ->

                                if (result.isSuccess) {
                                    _csvTable.value = SubmitResult.Success(body)
                                } else {
                                    when (val error = result.exceptionOrNull()) {
                                        is NetworkError.ServerError -> {
                                            Log.d(TAG, "Error while fetching tag serial data")
                                            _csvTable.value = SubmitResult.FailureServerError
                                        }

                                        is NetworkError.NoConnection -> {
                                            _csvTable.value = SubmitResult.FailureNoConnection
                                        }

                                        is NetworkError.ApiError -> {
                                            when (error.errorResponse.code) {
                                                401, 405 -> {
                                                    Log.d(
                                                        TOKEN,
                                                        "invalid token detected login out user"
                                                    )
                                                    _csvTable.value =
                                                        SubmitResult.InvalidApiToken(
                                                            error.errorResponse.code ?: 0,
                                                            error.errorResponse.message ?: ""
                                                        )
                                                }

                                                else -> {
                                                    _csvTable.value =
                                                        SubmitResult.FailureApiError(
                                                            error.errorResponse.message ?: ""
                                                        )
                                                    Log.d(
                                                        TAG,
                                                        "api error ${error.errorResponse.message}"
                                                    )
                                                }
                                            }
                                        }

                                        else -> {}
                                    }
                                }
                            }

                        } else {
                            _csvTable.value =
                                SubmitResult.FailureApiError(
                                    ContextCompat.getString(
                                        context,
                                        R.string.please_select_one_tag
                                    )
                                )
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "getCsvData: ${e.message} ${e.cause}")
                        _csvTable.value =
                            SubmitResult.FailureApiError(
                                ContextCompat.getString(
                                    context,
                                    R.string.formatting_error
                                )
                            )
                    }
                }
            } else {
                _csvTable.value =
                    SubmitResult.FailureApiError(
                        ContextCompat.getString(
                            context,
                            R.string.please_select_dates
                        )
                    )
            }
        }
    }

    suspend fun fetchCsvData(): ByteArray? {
        return repository.fetchedStoredCsvData()
    }

    fun internetAvailable(): Boolean {
        return repository.isInternetAvailable()
    }
}