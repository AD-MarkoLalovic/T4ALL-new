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
import com.mobility.enp.data.model.api_tool_history.v2base_model.DataValidation
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponse
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponseCroatia
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponseCroatiaResult
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponseResult
import com.mobility.enp.data.model.cardsweb.CardWebModel
import com.mobility.enp.data.model.csv_table.CsvModel
import com.mobility.enp.data.model.franchise.FranchiseModel
import com.mobility.enp.data.model.pdf_table.CsvTable
import com.mobility.enp.data.model.pdf_table.FilterPdf
import com.mobility.enp.data.repository.PassageHistoryRepository
import com.mobility.enp.data.room.PdfDaoHistory
import com.mobility.enp.data.room.api_related_daos.ToolHistoryV2AllowedCountryDao
import com.mobility.enp.data.room.api_related_daos.ToolHistoryV2Dao
import com.mobility.enp.data.room.api_related_daos.ToolHistoryV2DaoCroatia
import com.mobility.enp.data.room.api_related_daos.ToolHistoryV2DaoCroatiaResult
import com.mobility.enp.data.room.api_related_daos.ToolHistoryV2DaoResult
import com.mobility.enp.data.room.api_related_daos.ToolHistoryV2TagsSerials
import com.mobility.enp.services.MyFirebaseMessagingService.Companion.CHANNEL_ID
import com.mobility.enp.services.MyFirebaseMessagingService.Companion.NOTIFICATION_ID
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SharedPreferencesHelper
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.toCroatianPassage
import com.mobility.enp.util.toCroatianPassageResult
import com.mobility.enp.util.toLocalDate
import com.mobility.enp.util.toV2HistoryTagResponseResult
import com.mobility.enp.view.CsvActivity
import com.mobility.enp.view.PdfHistoryActivity
import com.mobility.enp.view.fragments.tool_history.HistoryFilterScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale

class UserPassViewModel(
    private val repository: PassageHistoryRepository,
    private val tagsDao: ToolHistoryV2TagsSerials,
    private val historyCroatiaPassageDao: ToolHistoryV2DaoCroatia,
    private val historyCroatiaPassageDaoResult: ToolHistoryV2DaoCroatiaResult,
    private val historyV2Dao: ToolHistoryV2Dao,
    private val historyV2DaoResult: ToolHistoryV2DaoResult,
    private val historyV2AllowedCountriesDao: ToolHistoryV2AllowedCountryDao,
    private val pdfExportDao: PdfDaoHistory
) : ViewModel() {

    companion object {
        const val TAG = "PassViewModel"
        const val TOKEN = "API_TOKEN"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepository = (this[APPLICATION_KEY] as MyApplication).passageHistoryRepository
                val tagsDao = (this[APPLICATION_KEY] as MyApplication).v2TagsDao
                val historyV2PassageDao = (this[APPLICATION_KEY] as MyApplication).v2HistoryDao
                val historyV2PassageDaoResult =
                    (this[APPLICATION_KEY] as MyApplication).v2HistoryDaoResult
                val historyCroatiaPassageDao = (this[APPLICATION_KEY] as MyApplication).v2CroatiaDao
                val historyCroatiaPassageDaoResult =
                    (this[APPLICATION_KEY] as MyApplication).v2CroatiaDaoResult
                val historyAllowedCountriesDao =
                    (this[APPLICATION_KEY] as MyApplication).v2AllowedCountriesDao
                val pdfExportDao =
                    (this[APPLICATION_KEY] as MyApplication).pdfExportDao
                UserPassViewModel(
                    repository = myRepository,
                    tagsDao,
                    historyCroatiaPassageDao,
                    historyCroatiaPassageDaoResult,
                    historyV2PassageDao,
                    historyV2PassageDaoResult,
                    historyAllowedCountriesDao,
                    pdfExportDao
                )
            }
        }
    }

    //important do not change .stateIn required for config changes so changes to ui persist
    val tagFlow = tagsDao.observeIndexData().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val tagFlowResult = tagsDao.observeIndexData()

    //important do not change .stateIn
    val allowedCountriesFlow = historyV2AllowedCountriesDao.observeAllowedCountries().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    suspend fun clearRoomData() {
        tagsDao.deleteData()
        historyV2Dao.deleteData()
        historyV2DaoResult.deleteData()
        historyCroatiaPassageDao.deleteData()
        historyCroatiaPassageDaoResult.deleteData()
        historyV2AllowedCountriesDao.clear()
        pdfExportDao.deleteData()
    }

    //important do not change .stateIn
    fun getV2PassagesBySerialAndCountryCode(
        serialNumber: String, countryCode: String
    ): StateFlow<List<V2HistoryTagResponse?>> {
        return historyV2Dao.observePassageDataBySerialAndCountryCode(serialNumber, countryCode)
            .stateIn(
                viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
            )
    }

    //important do not change .stateIn
    fun getV2PassagesBySerialAndCountryCodeResult(
        serialNumber: String, countryCode: String
    ): StateFlow<List<V2HistoryTagResponseResult?>> {
        return historyV2DaoResult.observePassageDataBySerialAndCountryCode(
            serialNumber, countryCode
        ).stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
    }

    fun getV2PassagesBySerialAndCountryCodeLoad(
        serialNumber: String, countryCode: String
    ): List<V2HistoryTagResponse?> {
        return historyV2Dao.observePassageDataBySerialAndCountryCodeLoad(serialNumber, countryCode)
    }

    fun getV2PassagesBySerialAndCountryCodeLoadResult(
        serialNumber: String, countryCode: String
    ): List<V2HistoryTagResponseResult?> {
        return historyV2DaoResult.observePassageDataBySerialAndCountryCodeLoad(
            serialNumber, countryCode
        )
    }

    //important do not change .stateIn
    fun getCroatiaPassagesBySerialPage(
        serialNumber: String, countryCode: String
    ): StateFlow<List<V2HistoryTagResponseCroatia?>> {
        return historyCroatiaPassageDao.observePassageDataBySerialCountry(serialNumber, countryCode)
            .stateIn(
                viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
            )
    }


    //important do not change .stateIn
    fun getCroatiaPassagesBySerialPageResult(
        serialNumber: String, countryCode: String
    ): StateFlow<List<V2HistoryTagResponseCroatiaResult?>> {
        return historyCroatiaPassageDaoResult.observePassageDataBySerialCountry(
            serialNumber, countryCode
        ).stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
    }

    fun getCroatiaPassagesBySerialPageLoad(
        serialNumber: String, countryCode: String
    ): List<V2HistoryTagResponseCroatia?> {
        return historyCroatiaPassageDao.observePassageDataBySerialCountryLoad(
            serialNumber, countryCode
        )
    }

    fun getCroatiaPassagesBySerialPageLoadResult(
        serialNumber: String, countryCode: String
    ): List<V2HistoryTagResponseCroatiaResult?> {
        return historyCroatiaPassageDaoResult.observePassageDataBySerialCountryLoad(
            serialNumber, countryCode
        )
    }

    private val _listOfCountriesMain = MutableStateFlow<List<String>>(emptyList())
    val listOfCountriesMainScreen: StateFlow<List<String>> get() = _listOfCountriesMain

    fun setAvailableCountriesMain(countries: List<String>) {
        this._listOfCountriesMain.value = countries
    }

    private val _availableCountryAdapterPosition = MutableStateFlow<Int>(0)
    val availableCountryAdapterPosition: StateFlow<Int> get() = _availableCountryAdapterPosition

    fun setCountryAdapterPosition(pos: Int) {
        _availableCountryAdapterPosition.value = pos
    }

    private val _availableCountryAdapterPositionFilter = MutableStateFlow<Int>(-1)
    val availableCountryAdapterPositionFilter: StateFlow<Int> get() = _availableCountryAdapterPositionFilter

    fun setCountryAdapterPositionFilter(pos: Int) {
        _availableCountryAdapterPositionFilter.value = pos
    }

    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    private val _userSelectedTags = MutableStateFlow<Set<Tag>>(emptySet())

    private var allowedCountriesForSerialAdapter: List<String> = emptyList()

    fun select(tag: Tag) {
        _userSelectedTags.update { it + tag }
        tag.id?.let { id ->
            _selectedTags.update { it + id }
        }
    }

    fun unselect(tag: Tag) {
        _userSelectedTags.update { it - tag }
        tag.id?.let { id ->
            _selectedTags.update { it - id }
        }
    }

    fun getSelectedTagList(): List<Tag> {
        return _userSelectedTags.value.toList()
    }

    fun isSelected(tag: Tag): Boolean {
        return tag.id?.let { _selectedTags.value.contains(it) } ?: false
    }

    private val _baseTagDataState =
        MutableStateFlow<SubmitResult<Pair<IndexData, CardWebModel?>>>(SubmitResult.Loading)
    val baseTagDataStateFirstScreen: StateFlow<SubmitResult<Pair<IndexData, CardWebModel?>>> get() = _baseTagDataState

    private val _baseTagDataStateByCountry =
        MutableStateFlow<SubmitResult<IndexData>>(SubmitResult.Loading)
    val baseTagDataStateByCountry: StateFlow<SubmitResult<IndexData>> get() = _baseTagDataStateByCountry


    private val _baseApiErrorsResultScreen =
        MutableStateFlow<SubmitResult<Unit>>(SubmitResult.Loading)
    val baseApiErrors: StateFlow<SubmitResult<Unit>> get() = _baseApiErrorsResultScreen


    private val _csvTable = MutableStateFlow<SubmitResult<CsvModel>>(SubmitResult.Empty)
    val csvTable: StateFlow<SubmitResult<CsvModel>> get() = _csvTable

    fun nullFlowState() {
        _csvTable.value = SubmitResult.Empty
        _pdfTable.value = SubmitResult.Empty
    }

    private val _pdfTable = MutableStateFlow<SubmitResult<ByteArray>>(SubmitResult.Empty)
    val pdfTable: StateFlow<SubmitResult<ByteArray>> get() = _pdfTable


    private val _complaintObjectionState =
        MutableStateFlow<SubmitResult<LostTagResponse>>(SubmitResult.Empty)
    val complaintObjectionState: StateFlow<SubmitResult<LostTagResponse>> get() = _complaintObjectionState

    private val _complaintObjectionStateResult =
        MutableStateFlow<SubmitResult<LostTagResponse>>(SubmitResult.Empty)
    val complaintObjectionStateResult: StateFlow<SubmitResult<LostTagResponse>> get() = _complaintObjectionStateResult

    var startDate = MutableLiveData<TimeSave>()
    var endDate = MutableLiveData<TimeSave>()

    private var userSelectedCalendarStart: Long? = null
    private var userSelectedCalendarEnd: Long? = null

    private val timeFrameFirstScreen: Long = 30

    var allTagsSelected = false

    var selectedCountry: String = ""

    fun nullData() {
        startDate.value = TimeSave(null, null)
        endDate.value = TimeSave(null, null)
        userSelectedCalendarStart = null
        userSelectedCalendarEnd = null
        selectedCountry = ""
        viewModelScope.launch {
            pdfExportDao.deleteData()
        }
    }

    fun resetUiState() {
        _baseTagDataState.value = SubmitResult.Empty
        _baseTagDataStateByCountry.value = SubmitResult.Empty
        _baseApiErrorsResultScreen.value = SubmitResult.Empty
        _csvTable.value = SubmitResult.Empty
        _complaintObjectionState.value = SubmitResult.Empty
        _complaintObjectionStateResult.value = SubmitResult.Empty
    }

    fun resetFilters() {
        _selectedTags.value = emptySet()
        _userSelectedTags.value = emptySet()
        selectedCountry = ""
        allTagsSelected = false
        _availableCountryAdapterPosition.value = -1
        _availableCountryAdapterPositionFilter.value = -1
    }

    fun resetDates() {
        startDate.value = TimeSave(null, null)
        endDate.value = TimeSave(null, null)
        userSelectedCalendarStart = null
        userSelectedCalendarEnd = null
    }

    fun resetAllState() {
        resetUiState()
        resetFilters()
        resetDates()
        _listOfCountriesMain.value = emptyList()
        allowedCountriesForSerialAdapter = emptyList()
    }

    private val itemsPerPage = 50
    private val tagsPerPage = 25

    fun isNetAvailable(): Boolean {
        return repository.isInternetAvailable()
    }

    fun getBaseDataAlternativeApi() {   // uses faster api call to get serial numbers of tags saving about 10 seconds on server response time
        _baseTagDataState.value = SubmitResult.Loading
        viewModelScope.launch(Dispatchers.IO) {

            val resultTags = async {
                repository.getTagBaseData(1, tagsPerPage)
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
                            "UserPassVM", "Error while fetching tags data", error
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
                                _baseTagDataState.value = SubmitResult.InvalidApiToken(
                                    error.errorResponse.code, error.errorResponse.message ?: ""
                                )
                            }

                            else -> {
                                _baseTagDataState.value = SubmitResult.FailureApiError(
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
                            "UserPassVM", "Error while fetching cards data", error
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
                                _baseTagDataState.value = SubmitResult.InvalidApiToken(
                                    error.errorResponse.code, error.errorResponse.message ?: ""
                                )
                            }

                            else -> {
                                _baseTagDataState.value = SubmitResult.FailureApiError(
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
                repository.getTagBaseData(1, tagsPerPage)
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
                            "UserPassVM", "Error while fetching tags data", error
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
                                _baseTagDataStateByCountry.value = SubmitResult.InvalidApiToken(
                                    error.errorResponse.code, error.errorResponse.message ?: ""
                                )
                            }

                            else -> {
                                _baseTagDataStateByCountry.value = SubmitResult.FailureApiError(
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

    fun getSerialDeviceDataValidationSerialAdapter(totalPages: Int) {
        viewModelScope.launch {
            val semaphore = Semaphore(20)

            withContext(Dispatchers.IO) {
                val result = coroutineScope {
                    (1..totalPages).map { page ->
                        async {
                            semaphore.withPermit {
                                try {
                                    val response = repository.getTagBaseData(page, tagsPerPage)

                                    response.getOrNull()
                                } catch (e: Exception) {
                                    Log.d(
                                        TAG,
                                        "getSerialDeviceDataValidationFirstScreen: ${e.toString()}"
                                    )
                                    null
                                }
                            }
                        }
                    }
                }.awaitAll().filterNotNull()

                if (result.isNotEmpty()) {
                    repository.roomUpsertAllIndexData(result)
                }
            }
        }
    }

    fun getSerialPassageTagDataValidation(totalPages: Int, tagSerial: String, countryCode: String) {
        viewModelScope.launch() {
            val semaphore = Semaphore(20)

            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
            val dateTo = LocalDate.now()
            val dateFrom = dateTo.minusDays(timeFrameFirstScreen)

            val dateToFormatted = dateTo.format(formatter)
            val dateFromFormatted = dateFrom.format(formatter)

            withContext(Dispatchers.IO) {
                val result = coroutineScope {
                    (1..totalPages).map { page ->
                        async {
                            semaphore.withPermit {
                                try {
                                    val response = repository.getAdapterPassageDataCountryFilter(
                                        tagSerial,
                                        countryCode,
                                        page,
                                        itemsPerPage,
                                        dateFromFormatted,
                                        dateToFormatted
                                    )

                                    val body = response.getOrNull()

                                    body?.copy(
                                        serial = tagSerial,
                                        countryCode = countryCode,
                                        currentPage = body.data?.records?.pagination?.currentPage
                                            ?: 0,
                                        lastPage = body.data?.records?.pagination?.lastPage ?: 0,
                                        totalRecords = body.data?.records?.pagination?.total ?: 0,
                                        perPage = body.data?.records?.pagination?.perPage ?: 0
                                    )

                                } catch (e: Exception) {
                                    Log.d(TAG, "getSerialPassageTagDataValidation: ${e.toString()}")
                                    null
                                }
                            }
                        }
                    }.awaitAll().filterNotNull()
                }

                if (result.isNotEmpty()) {
                    repository.roomUpsertAllV2Passages(result)
                }
            }
        }
    }

    fun getSerialPassageTagDataValidationResult(
        totalPages: Int, tagSerial: String, countryCode: String
    ) {
        viewModelScope.launch() {
            val semaphore = Semaphore(20)

            withContext(Dispatchers.IO) {
                val result = coroutineScope {
                    (1..totalPages).map { page ->
                        async {
                            semaphore.withPermit {
                                try {
                                    val response = repository.getAdapterPassageDataCountryFilter(
                                        tagSerial,
                                        countryCode,
                                        page,
                                        itemsPerPage,
                                        startDate.value?.formattedTime ?: "",
                                        endDate.value?.formattedTime ?: ""
                                    )

                                    val body = response.getOrNull()

                                    body?.copy(
                                        serial = tagSerial,
                                        countryCode = countryCode,
                                        currentPage = body.data?.records?.pagination?.currentPage
                                            ?: 0,
                                        lastPage = body.data?.records?.pagination?.lastPage ?: 0,
                                        totalRecords = body.data?.records?.pagination?.total ?: 0,
                                        perPage = body.data?.records?.pagination?.perPage ?: 0
                                    )

                                } catch (e: Exception) {
                                    Log.d(TAG, "getSerialPassageTagDataValidation: ${e.toString()}")
                                    null
                                }
                            }
                        }
                    }.awaitAll().filterNotNull()
                }

                if (result.isNotEmpty()) {
                    val listTransformed: ArrayList<V2HistoryTagResponseResult> = arrayListOf()
                    for (data in result) {
                        listTransformed.add(data.toV2HistoryTagResponseResult())
                    }
                    repository.roomUpsertAllV2PassagesResult(listTransformed.toList())
                }
            }
        }
    }

    fun getSerialPassageTagDataValidationCroatia(
        totalPages: Int, tagSerial: String, countryCode: String
    ) {
        viewModelScope.launch() {
            val semaphore = Semaphore(20)

            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
            val dateTo = LocalDate.now()
            val dateFrom = dateTo.minusDays(timeFrameFirstScreen)

            val dateToFormatted = dateTo.format(formatter)
            val dateFromFormatted = dateFrom.format(formatter)

            withContext(Dispatchers.IO) {
                val result = coroutineScope {
                    (1..totalPages).map { page ->
                        async {
                            semaphore.withPermit {
                                try {
                                    val response = repository.getAdapterPassageDataCountryFilter(
                                        tagSerial,
                                        countryCode,
                                        page,
                                        itemsPerPage,
                                        dateFromFormatted,
                                        dateToFormatted
                                    )

                                    val body = response.getOrNull()

                                    body?.copy(
                                        serial = tagSerial,
                                        countryCode = countryCode,
                                        currentPage = body.data?.records?.pagination?.currentPage
                                            ?: 0,
                                        lastPage = body.data?.records?.pagination?.lastPage ?: 0,
                                        totalRecords = body.data?.records?.pagination?.total ?: 0,
                                        perPage = body.data?.records?.pagination?.perPage ?: 0
                                    )

                                } catch (e: Exception) {
                                    Log.d(
                                        TAG,
                                        "getSerialPassageTagDataValidationCroatia: ${e.toString()}"
                                    )
                                    null
                                }
                            }
                        }
                    }.awaitAll().filterNotNull()
                }

                if (result.isNotEmpty()) {
                    val convertedList: ArrayList<V2HistoryTagResponseCroatia> = arrayListOf()
                    for (data in result) {
                        convertedList.add(data.toCroatianPassage())
                    }
                    repository.roomUpsertAllV2PassagesCroatia(convertedList.toList())
                }
            }
        }
    }

    fun getSerialPassageTagDataValidationCroatiaResult(
        totalPages: Int, tagSerial: String, countryCode: String
    ) {
        viewModelScope.launch() {
            val semaphore = Semaphore(20)

            withContext(Dispatchers.IO) {
                val result = coroutineScope {
                    (1..totalPages).map { page ->
                        async {
                            semaphore.withPermit {
                                try {
                                    val response = repository.getAdapterPassageDataCountryFilter(
                                        tagSerial,
                                        countryCode,
                                        page,
                                        itemsPerPage,
                                        startDate.value?.formattedTime ?: "",
                                        endDate.value?.formattedTime ?: ""
                                    )

                                    val body = response.getOrNull()

                                    body?.copy(
                                        serial = tagSerial,
                                        countryCode = countryCode,
                                        currentPage = body.data?.records?.pagination?.currentPage
                                            ?: 0,
                                        lastPage = body.data?.records?.pagination?.lastPage ?: 0,
                                        totalRecords = body.data?.records?.pagination?.total ?: 0,
                                        perPage = body.data?.records?.pagination?.perPage ?: 0
                                    )

                                } catch (e: Exception) {
                                    Log.d(
                                        TAG,
                                        "getSerialPassageTagDataValidationCroatia: ${e.toString()}"
                                    )
                                    null
                                }
                            }
                        }
                    }.awaitAll().filterNotNull()
                }

                if (result.isNotEmpty()) {
                    val convertedList: ArrayList<V2HistoryTagResponseCroatiaResult> = arrayListOf()
                    for (data in result) {
                        convertedList.add(data.toCroatianPassageResult())
                    }
                    repository.roomUpsertAllV2PassagesCroatiaResult(convertedList.toList())
                }
            }
        }
    }

    fun saveRoomTagDataFirstScreen(indexData: IndexData) {
        val currentPage = indexData.data?.currentPage ?: 0
        val lastPage = indexData.data?.lastPage ?: 0
        val total = indexData.data?.total ?: 0
        indexData.setPages(currentPage, lastPage, total, allowedCountriesForSerialAdapter)

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.upsertBaseTagData(indexData)
            }
        }
    }

    fun deleteOldResultData() {
        viewModelScope.launch(Dispatchers.IO) {
            historyV2DaoResult.deleteData()
            historyCroatiaPassageDaoResult.deleteData()
        }
    }

    fun saveAllowedCountries(countries: List<String>) {
        allowedCountriesForSerialAdapter = countries
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.roomUpsertAllowedCountries(countries)
            }
        }
    }

    fun postComplaint(complaintBody: ComplaintBody, dataValidation: DataValidation) {
        _complaintObjectionState.value = SubmitResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.postComplaint(complaintBody)
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data == null) {
                    _complaintObjectionState.value = SubmitResult.Empty
                } else {
                    getSerialPassageTagDataValidation(
                        dataValidation.totalPages,
                        dataValidation.tagSerialNumber,
                        dataValidation.countryCode
                    )
                    delay(2000)
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
                                _baseTagDataState.value = SubmitResult.InvalidApiToken(
                                    error.errorResponse.code, error.errorResponse.message ?: ""
                                )
                            }

                            else -> {
                                _baseTagDataState.value = SubmitResult.FailureApiError(
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

    fun postObjection(objectionBody: ObjectionBody, dataValidation: DataValidation) {
        _complaintObjectionState.value = SubmitResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.postObjection(objectionBody)
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data == null) {
                    _complaintObjectionState.value = SubmitResult.Empty
                } else {
                    getSerialPassageTagDataValidation(
                        dataValidation.totalPages,
                        dataValidation.tagSerialNumber,
                        dataValidation.countryCode
                    )
                    delay(2000)
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
                                _baseTagDataState.value = SubmitResult.InvalidApiToken(
                                    error.errorResponse.code, error.errorResponse.message ?: ""
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


    fun postComplaintResult(complaintBody: ComplaintBody, dataValidation: DataValidation) {
        _complaintObjectionStateResult.value = SubmitResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.postComplaint(complaintBody)
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data == null) {
                    _complaintObjectionStateResult.value = SubmitResult.Empty
                } else {
                    getSerialPassageTagDataValidationResult(
                        dataValidation.totalPages,
                        dataValidation.tagSerialNumber,
                        dataValidation.countryCode
                    )
                    delay(2000)
                    _complaintObjectionStateResult.value = SubmitResult.Success(data)
                }
            } else {
                when (val error = result.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.d(TAG, "Error while fetching tag serial data")
                        _baseApiErrorsResultScreen.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _baseApiErrorsResultScreen.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(
                                    "API_TOKEN UserPassViewModel",
                                    "invalid token detected login out user"
                                )
                                _baseApiErrorsResultScreen.value = SubmitResult.InvalidApiToken(
                                    error.errorResponse.code, error.errorResponse.message ?: ""
                                )
                            }

                            else -> {
                                _baseApiErrorsResultScreen.value = SubmitResult.FailureApiError(
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

    fun postObjectionResult(objectionBody: ObjectionBody, dataValidation: DataValidation) {
        _complaintObjectionStateResult.value = SubmitResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.postObjection(objectionBody)
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data == null) {
                    _complaintObjectionStateResult.value = SubmitResult.Empty
                } else {
                    getSerialPassageTagDataValidationResult(
                        dataValidation.totalPages,
                        dataValidation.tagSerialNumber,
                        dataValidation.countryCode
                    )
                    delay(2000)
                    _complaintObjectionStateResult.value = SubmitResult.Success(data)
                }
            } else {
                when (val error = result.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.d(TAG, "Error while fetching tag serial data")
                        _baseApiErrorsResultScreen.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _baseApiErrorsResultScreen.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(
                                    "API_TOKEN UserPassViewModel",
                                    "invalid token detected login out user"
                                )
                                _baseApiErrorsResultScreen.value = SubmitResult.InvalidApiToken(
                                    error.errorResponse.code, error.errorResponse.message ?: ""
                                )
                            }

                            else -> {
                                _baseApiErrorsResultScreen.value =
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
     * for first screen
     * uses limited time range and that is the difference
     * always takes last 30 days , result screen takes user given range
     * saves in room
     * observes with flow change in adapter
     */
    fun getToolHistoryTransit(
        tagSerialNumber: String, currentPage: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val formatter = if (selectedCountry.equals("HR", ignoreCase = true)) {
                DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
            } else {
                DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
            }  // they changed it to be the same and didn't notify mobile it was dd/MM/yyyy for croatia

            val dateTo = LocalDate.now()
            val dateFrom = dateTo.minusDays(timeFrameFirstScreen)

            val dateToFormatted = dateTo.format(formatter)
            val dateFromFormatted = dateFrom.format(formatter)

            var result = repository.getAdapterPassageData(
                tagSerialNumber,
                currentPage,
                itemsPerPage,
                dateFromFormatted ?: "",
                dateToFormatted ?: ""
            )

            if (!selectedCountry.isEmpty()) {
                result = repository.getAdapterPassageDataCountryFilter(
                    tagSerialNumber,
                    selectedCountry,
                    currentPage,
                    itemsPerPage,
                    dateFromFormatted ?: "",
                    dateToFormatted ?: ""
                )
            }
            if (result.isSuccess) {
                result.getOrNull()?.let { v2HistoryTagResponse ->
                    v2HistoryTagResponse.countryCode = selectedCountry
                    v2HistoryTagResponse.serial = tagSerialNumber
                    v2HistoryTagResponse.currentPage =
                        v2HistoryTagResponse.data?.records?.pagination?.currentPage ?: 0
                    v2HistoryTagResponse.lastPage =
                        v2HistoryTagResponse.data?.records?.pagination?.lastPage ?: 0
                    v2HistoryTagResponse.totalRecords =
                        v2HistoryTagResponse.data?.records?.pagination?.total ?: 0
                    v2HistoryTagResponse.perPage =
                        v2HistoryTagResponse.data?.records?.pagination?.perPage ?: 0

                    roomPassageDataFirstScreen(v2HistoryTagResponse)
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
                                _baseTagDataState.value = SubmitResult.InvalidApiToken(
                                    error.errorResponse.code ?: 0, error.errorResponse.message ?: ""
                                )
                            }

                            else -> {
                                _baseTagDataState.value = SubmitResult.FailureApiError(
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

    fun getToolHistoryTransitResult(
        tagSerialNumber: String, currentPage: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
            val dateTo = endDate.value?.inDateForm?.toLocalDate() ?: LocalDate.now()
            val dateFrom = dateTo.minusDays(timeFrameFirstScreen)

            val dateToFormatted = dateTo.format(formatter)
            val dateFromFormatted = dateFrom.format(formatter)


            var result = repository.getAdapterPassageData(
                tagSerialNumber,
                currentPage,
                itemsPerPage,
                startDate.value?.formattedTime ?: dateFromFormatted,
                endDate.value?.formattedTime ?: dateToFormatted
            )

            if (!selectedCountry.isEmpty()) {
                result = repository.getAdapterPassageDataCountryFilter(
                    tagSerialNumber,
                    selectedCountry,
                    currentPage,
                    itemsPerPage,
                    startDate.value?.formattedTime ?: dateFromFormatted,
                    endDate.value?.formattedTime ?: dateToFormatted
                )
            }
            if (result.isSuccess) {
                result.getOrNull()?.let { v2HistoryTagResponse ->
                    v2HistoryTagResponse.countryCode = selectedCountry
                    v2HistoryTagResponse.serial = tagSerialNumber
                    v2HistoryTagResponse.currentPage =
                        v2HistoryTagResponse.data?.records?.pagination?.currentPage ?: 0
                    v2HistoryTagResponse.lastPage =
                        v2HistoryTagResponse.data?.records?.pagination?.lastPage ?: 0
                    v2HistoryTagResponse.totalRecords =
                        v2HistoryTagResponse.data?.records?.pagination?.total ?: 0
                    v2HistoryTagResponse.perPage =
                        v2HistoryTagResponse.data?.records?.pagination?.perPage ?: 0

                    roomPassageDataResultScreen(v2HistoryTagResponse)
                }

            } else {
                when (val error = result.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.d(TAG, "Error while fetching tag serial data")
                        _baseApiErrorsResultScreen.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _baseApiErrorsResultScreen.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(TOKEN, "invalid token detected login out user")
                                _baseApiErrorsResultScreen.value = SubmitResult.InvalidApiToken(
                                    error.errorResponse.code ?: 0, error.errorResponse.message ?: ""
                                )
                            }

                            else -> {
                                _baseApiErrorsResultScreen.value = SubmitResult.FailureApiError(
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


    fun getToolHistoryTransitCroatia(
        tagSerialNumber: String, currentPage: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
            val dateTo = LocalDate.now()
            val dateFrom = dateTo.minusDays(timeFrameFirstScreen)

            val dateToFormatted = dateTo.format(formatter)
            val dateFromFormatted = dateFrom.format(formatter)

            val result = repository.getAdapterPassageDataCountryFilter(
                tagSerialNumber,
                "HR",
                currentPage,
                itemsPerPage,
                dateFromFormatted ?: "",
                dateToFormatted ?: ""
            )


            if (result.isSuccess) {
                result.getOrNull()?.let { v2HistoryTagResponse ->
                    v2HistoryTagResponse.countryCode = selectedCountry
                    v2HistoryTagResponse.serial = tagSerialNumber
                    v2HistoryTagResponse.currentPage =
                        v2HistoryTagResponse.data?.records?.pagination?.currentPage ?: 0
                    v2HistoryTagResponse.lastPage =
                        v2HistoryTagResponse.data?.records?.pagination?.lastPage ?: 0
                    v2HistoryTagResponse.totalRecords =
                        v2HistoryTagResponse.data?.records?.pagination?.total ?: 0
                    v2HistoryTagResponse.perPage =
                        v2HistoryTagResponse.data?.records?.pagination?.perPage ?: 0
                    roomPassageDataFirstScreenCroatia(v2HistoryTagResponse)
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
                                _baseTagDataState.value = SubmitResult.InvalidApiToken(
                                    error.errorResponse.code ?: 0, error.errorResponse.message ?: ""
                                )
                            }

                            else -> {
                                _baseTagDataState.value = SubmitResult.FailureApiError(
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

    fun getToolHistoryTransitCroatiaResult(
        tagSerialNumber: String, currentPage: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
            val dateTo = endDate.value?.inDateForm?.toLocalDate() ?: LocalDate.now()
            val dateFrom = dateTo.minusDays(timeFrameFirstScreen)

            val dateToFormatted = dateTo.format(formatter)
            val dateFromFormatted = dateFrom.format(formatter)


            val result = repository.getAdapterPassageDataCountryFilter(
                tagSerialNumber,
                "HR",
                currentPage,
                itemsPerPage,
                startDate.value?.formattedTime ?: dateFromFormatted,
                endDate.value?.formattedTime ?: dateToFormatted
            )


            if (result.isSuccess) {
                result.getOrNull()?.let { v2HistoryTagResponse ->
                    v2HistoryTagResponse.countryCode = selectedCountry
                    v2HistoryTagResponse.serial = tagSerialNumber
                    v2HistoryTagResponse.currentPage =
                        v2HistoryTagResponse.data?.records?.pagination?.currentPage ?: 0
                    v2HistoryTagResponse.lastPage =
                        v2HistoryTagResponse.data?.records?.pagination?.lastPage ?: 0
                    v2HistoryTagResponse.totalRecords =
                        v2HistoryTagResponse.data?.records?.pagination?.total ?: 0
                    v2HistoryTagResponse.perPage =
                        v2HistoryTagResponse.data?.records?.pagination?.perPage ?: 0
                    roomPassageDataFirstScreenCroatiaResult(v2HistoryTagResponse)
                }

            } else {
                when (val error = result.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.d(TAG, "Error while fetching tag serial data")
                        _baseApiErrorsResultScreen.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _baseApiErrorsResultScreen.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(TOKEN, "invalid token detected login out user")
                                _baseApiErrorsResultScreen.value = SubmitResult.InvalidApiToken(
                                    error.errorResponse.code ?: 0, error.errorResponse.message ?: ""
                                )
                            }

                            else -> {
                                _baseApiErrorsResultScreen.value = SubmitResult.FailureApiError(
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

    fun getTagsUpdate(nextPage: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val resultTags = repository.getTagBaseData(nextPage, tagsPerPage)
            if (resultTags.isSuccess) {
                resultTags.getOrNull()?.let { indexData ->
                    saveRoomTagDataFirstScreen(indexData)
                }
            }
        }
    }

    fun formatPassageDate(checkInDate: String?): String? {
        if (checkInDate.isNullOrBlank()) return null

        return try {
            val inputFormatter = DateTimeFormatter.ofPattern(
                "yyyy-MM-dd HH:mm:ss",
                Locale.ENGLISH
            )

            val outputFormatter = DateTimeFormatter.ofPattern(
                "dd.MM.yyyy. HH:mm",
                Locale.ENGLISH
            )

            val dateTime = LocalDateTime.parse(checkInDate, inputFormatter)
            dateTime.format(outputFormatter)

        } catch (e: DateTimeParseException) {
            null
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
        val pendingIntent = PendingIntent.getActivity(
            repository.fetchContext(), 100, intent, PendingIntent.FLAG_IMMUTABLE
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


    fun postNotificationPDF() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Tool4all", NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "Tool4all"
        channel.enableLights(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        channel.lightColor = Color.BLUE

        val intent = Intent(repository.fetchContext(), PdfHistoryActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            repository.fetchContext(), 100, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager =
            repository.fetchContext().getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(
            repository.fetchContext(), CHANNEL_ID
        ).setSmallIcon(R.drawable.splash_logo)
            .setContentTitle(repository.fetchContext().getString(R.string.export_pdf))
            .setContentIntent(pendingIntent)
            .setContentText(repository.fetchContext().getString(R.string.export_pdf))
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
                                HistoryFilterScreen.TAG,
                                "CSV file saved successfully in Documents folder."
                            )
                        } ?: Log.d(HistoryFilterScreen.TAG, "Failed to open OutputStream.")
                } ?: Log.d(
                    HistoryFilterScreen.TAG, "Failed to create file URI in MediaStore."
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
                    billNumber, timeOfPassage, payRamp, price
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
                                HistoryFilterScreen.TAG,
                                "PDF file saved successfully in Documents folder."
                            )
                        } ?: Log.d(HistoryFilterScreen.TAG, "Failed to open OutputStream.")
                } ?: Log.d(
                    HistoryFilterScreen.TAG, "Failed to create file URI in MediaStore."
                )

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var _data: MutableLiveData<IndexData> = MutableLiveData<IndexData>()
    val data: LiveData<IndexData> get() = _data

    var selectedTags: ArrayList<Tag> = ArrayList()
    var tagForExport: Tag? = null

    suspend fun insertRoomToolHistoryIndexData(indexData: IndexData) {
        repository.upsertBaseTagData(indexData)
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

            val constraintsBuilder =
                CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now())


            franchiseModel?.let { model ->
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(context.getString(R.string.select_date))
                    .setSelection(selectedDate).setCalendarConstraints(constraintsBuilder.build())
                    .setNegativeButtonText(context.getString(R.string.cancel))
                    .setPositiveButtonText(context.getString(R.string.confirm))
                    .setTheme(model.franchiseCalendarStyle).build()

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
                    .setSelection(selectedDate).setCalendarConstraints(constraintsBuilder.build())
                    .setNegativeButtonText(context.getString(R.string.cancel))
                    .setPositiveButtonText(context.getString(R.string.confirm)).build()

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

    fun roomPassageDataFirstScreen(data: V2HistoryTagResponse) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.roomUpsertV2Passages(data)
        }
    }

    fun roomPassageDataResultScreen(data: V2HistoryTagResponse) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.roomUpsertV2PassagesResult(data)
        }
    }

    fun roomPassageDataFirstScreenCroatia(data: V2HistoryTagResponse) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.roomUpsertCroatianPassage(data)
        }
    }

    fun roomPassageDataFirstScreenCroatiaResult(data: V2HistoryTagResponse) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.roomUpsertCroatianPassageResult(data)
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
                        context, context.getString(R.string.end_date_check), Toast.LENGTH_SHORT
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
                                tagSerial, dateStartApi, dateEndApi, selectedCountry
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
                                                    _csvTable.value = SubmitResult.InvalidApiToken(
                                                        error.errorResponse.code ?: 0,
                                                        error.errorResponse.message ?: ""
                                                    )
                                                }

                                                else -> {
                                                    _csvTable.value = SubmitResult.FailureApiError(
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
                            _csvTable.value = SubmitResult.FailureApiError(
                                ContextCompat.getString(
                                    context, R.string.please_select_one_tag
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "getCsvData: ${e.message} ${e.cause}")
                        _csvTable.value = SubmitResult.FailureApiError(
                            ContextCompat.getString(
                                context, R.string.formatting_error
                            )
                        )
                    }
                }
            } else {
                _csvTable.value = SubmitResult.FailureApiError(
                    ContextCompat.getString(
                        context, R.string.please_select_dates
                    )
                )
            }
        }
    }

    fun getPDFData(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            if (startDate.value?.inDateForm?.time != null && endDate.value?.inDateForm?.time != null) {
                if (endDate.value?.inDateForm!!.before(startDate.value?.inDateForm!!)) {
                    Toast.makeText(
                        context, context.getString(R.string.end_date_check), Toast.LENGTH_SHORT
                    ).show()
                    _pdfTable.value = SubmitResult.Empty
                } else {
                    try {
                        _pdfTable.value = SubmitResult.Loading
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

                            val result = repository.getPDFTableData(
                                tagSerial, dateStartApi, dateEndApi, selectedCountry
                            )

                            val body = result.getOrNull()
                            body?.let { data ->

                                if (result.isSuccess) {
                                    _pdfTable.value = SubmitResult.Success(data)

                                    pdfExportDao.deleteData()
                                    pdfExportDao.upsertData(FilterPdf(0, "my_pdf", data))

                                } else {
                                    when (val error = result.exceptionOrNull()) {
                                        is NetworkError.ServerError -> {
                                            Log.d(TAG, "Error while fetching tag serial data")
                                            _pdfTable.value = SubmitResult.FailureServerError
                                        }

                                        is NetworkError.NoConnection -> {
                                            _pdfTable.value = SubmitResult.FailureNoConnection
                                        }

                                        is NetworkError.ApiError -> {
                                            when (error.errorResponse.code) {
                                                401, 405 -> {
                                                    Log.d(
                                                        TOKEN,
                                                        "invalid token detected login out user"
                                                    )
                                                    _pdfTable.value = SubmitResult.InvalidApiToken(
                                                        error.errorResponse.code ?: 0,
                                                        error.errorResponse.message ?: ""
                                                    )
                                                }

                                                else -> {
                                                    _pdfTable.value = SubmitResult.FailureApiError(
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
                            _pdfTable.value = SubmitResult.FailureApiError(
                                ContextCompat.getString(
                                    context, R.string.please_select_one_tag
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "getCsvData: ${e.message} ${e.cause}")
                        _pdfTable.value = SubmitResult.FailureApiError(
                            ContextCompat.getString(
                                context, R.string.formatting_error
                            )
                        )
                    }
                }
            } else {
                _pdfTable.value = SubmitResult.FailureApiError(
                    ContextCompat.getString(
                        context, R.string.please_select_dates
                    )
                )
            }
        }
    }

    suspend fun fetchCsvData(): ByteArray? {
        return repository.fetchedStoredCsvData()
    }

    suspend fun fetchPDFData(): ByteArray? {
        return repository.fetchedStoredPDFData()
    }

    fun internetAvailable(): Boolean {
        return repository.isInternetAvailable()
    }
}