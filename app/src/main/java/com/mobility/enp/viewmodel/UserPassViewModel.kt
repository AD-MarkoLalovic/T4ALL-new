package com.mobility.enp.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.mobility.enp.data.model.franchise.FranchiseModel
import com.mobility.enp.data.repository.PassageHistoryRepository
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SharedPreferencesHelper
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.toCroatianPassage
import com.mobility.enp.util.toCroatianPassageResult
import com.mobility.enp.util.toLocalDate
import com.mobility.enp.util.toV2HistoryTagResponseResult
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
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale

class UserPassViewModel(
    private val repository: PassageHistoryRepository,
) : ViewModel() {

    companion object {
        const val TAG = "PassViewModel"
        const val TOKEN = "API_TOKEN"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val repository = (this[APPLICATION_KEY] as MyApplication).passageHistoryRepository
                UserPassViewModel(
                    repository = repository,
                )
            }
        }
    }

    //important do not change .stateIn required for config changes so changes to ui persist
    val tagFlow = repository.getTagFlow()

    val tagFlowResult = repository.getTagFlow()

    val allowedCountriesFlow = repository.getAllowedCountriesFlow()

    fun clearRoomData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTagData()
            repository.deleteCroatiaPassages()
            repository.deleteCroatiaResult()
            repository.clearAllowedCountriesFlow()
            repository.deletePdfExportData()
            repository.deleteV2DaoResult()
            repository.deleteV2PassageData()
        }
    }


    fun getV2PassagesBySerialAndCountryCode(
        serialNumber: String,
        countryCode: String
    ): StateFlow<List<V2HistoryTagResponse?>> {
        return repository.getV2PassagesBySerialAndCountryCode(
            serialNumber,
            countryCode
        ).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun getV2PassagesBySerialAndCountryCodeResult(
        serialNumber: String,
        countryCode: String
    ): StateFlow<List<V2HistoryTagResponseResult?>> {
        return repository.getV2PassagesBySerialAndCountryCodeResult(
            serialNumber,
            countryCode
        ).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun getPassageBySerialCodeCountry(
        serialNumber: String, countryCode: String
    ): List<V2HistoryTagResponse?> {
        return repository.getV2PassagesBySerialAndCountryCodeLoad(serialNumber, countryCode)
    }

    fun getPassageBySerialNumberCode(
        serialNumber: String, countryCode: String
    ): List<V2HistoryTagResponseResult?> {
        return repository.getV2PassagesBySerialAndCountryCodeLoadResult(serialNumber, countryCode)
    }

    fun getCroatiaPassagesBySerialPage(
        serialNumber: String,
        countryCode: String
    ): StateFlow<List<V2HistoryTagResponseCroatia?>> {
        return repository.getCroatiaPassagesBySerialPage(
            serialNumber,
            countryCode
        ).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun getCroatiaPassagesBySerialPageResult(
        serialNumber: String,
        countryCode: String
    ): StateFlow<List<V2HistoryTagResponseCroatiaResult?>> {
        return repository.getCroatiaPassagesBySerialPageResult(
            serialNumber,
            countryCode
        ).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun getCPassagesBySerialCountry(
        serialNumber: String, countryCode: String
    ): List<V2HistoryTagResponseCroatia?> {
        return repository.getCroatiaPassagesBySerialPageLoad(serialNumber, countryCode)
    }

    fun getCPassagesResultBySerialCode(
        serialNumber: String, countryCode: String
    ): List<V2HistoryTagResponseCroatiaResult?> {
        return repository.getCroatiaPassagesBySerialPageLoadResult(serialNumber, countryCode)
    }

    private val _listOfCountriesMain = MutableStateFlow<List<String>>(emptyList())
    val listOfCountriesMainScreen: StateFlow<List<String>> get() = _listOfCountriesMain

    fun setAvailableCountriesMain(countries: List<String>) {
        this._listOfCountriesMain.value = countries
    }

    private val _availableCountryAdapterPositionFilter = MutableStateFlow<Int>(-1)
    val availableCountryAdapterPositionFilter: StateFlow<Int> get() = _availableCountryAdapterPositionFilter

    fun setCountryAdapterPositionFilter(pos: Int) {
        _availableCountryAdapterPositionFilter.value = pos
    }

    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    private val _userSelectedTags = MutableStateFlow<Set<Tag>>(emptySet())

    var selectedTags: ArrayList<Tag> = ArrayList()

    fun clearSelectedTags() {
        _selectedTags.value = emptySet()
        _userSelectedTags.value = emptySet()
        selectedTags.clear()
    }

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
        MutableStateFlow<SubmitResult<Pair<IndexData, V2HistoryTagResponse?>>>(SubmitResult.Loading)
    val baseTagDataStateFirstScreen: StateFlow<SubmitResult<Pair<IndexData, V2HistoryTagResponse?>>> get() = _baseTagDataState

    private val _baseTagDataStateByCountry =
        MutableStateFlow<SubmitResult<IndexData>>(SubmitResult.Loading)
    val baseTagDataStateByCountry: StateFlow<SubmitResult<IndexData>> get() = _baseTagDataStateByCountry


    private val _baseApiErrorsResultScreen =
        MutableStateFlow<SubmitResult<Unit>>(SubmitResult.Loading)
    val baseApiErrors: StateFlow<SubmitResult<Unit>> get() = _baseApiErrorsResultScreen

    fun nullFlowState() {
        // Can be used to reset other states if needed
    }

    private val _noPassages = MutableLiveData<Boolean?>(null)
    val noPassages: LiveData<Boolean?> get() = _noPassages

    private val _complaintObjectionState =
        MutableStateFlow<SubmitResult<LostTagResponse>>(SubmitResult.Empty)
    val complaintObjectionState: StateFlow<SubmitResult<LostTagResponse>> get() = _complaintObjectionState

    private val _complaintObjectionStateResult =
        MutableStateFlow<SubmitResult<LostTagResponse>>(SubmitResult.Empty)
    val complaintObjectionStateResult: StateFlow<SubmitResult<LostTagResponse>> get() = _complaintObjectionStateResult

    var startDate = MutableLiveData<TimeSave>()
    var endDate = MutableLiveData<TimeSave>()

    var userSelectedCalendarStart: Long? = null
    var userSelectedCalendarEnd: Long? = null

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
            repository.deletePdfExportData()
        }
        _noPassages.value = null
    }

    fun resetUiState() {
        _baseTagDataState.value = SubmitResult.Empty
        _baseTagDataStateByCountry.value = SubmitResult.Empty
        _baseApiErrorsResultScreen.value = SubmitResult.Empty
        _complaintObjectionState.value = SubmitResult.Empty
        _complaintObjectionStateResult.value = SubmitResult.Empty
    }

    fun resetFilters() {
        _selectedTags.value = emptySet()
        _userSelectedTags.value = emptySet()
        selectedCountry = ""
        allTagsSelected = false
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

            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
            val dateTo = LocalDate.now()
            val dateFrom = dateTo.minusDays(timeFrameFirstScreen)

            val dateToFormatted = dateTo.format(formatter)
            val dateFromFormatted = dateFrom.format(formatter)

            val resultTags = async {
                repository.getTagBaseData(1, tagsPerPage)
            }

            val resultCards = async {
                repository.getAdapterAllowedCountries(
                    1,
                    itemsPerPage,
                    dateFromFormatted,
                    dateToFormatted
                )
            }

            val tagResultDeferred = resultTags.await()
            val resultCardsDeferred = resultCards.await()

            if (tagResultDeferred.isSuccess && resultCardsDeferred.isSuccess) {

                val tagsData = tagResultDeferred.getOrNull()
                val v2Response = resultCardsDeferred.getOrNull()

                if (tagsData == null || v2Response == null) {
                    _baseTagDataState.value = SubmitResult.Empty
                } else {
                    _baseTagDataState.value = SubmitResult.Success(Pair(tagsData, v2Response))
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
            repository.deleteV2DaoResult()
            repository.deleteCroatiaResult()
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
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)

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

    fun internetAvailable(): Boolean {
        return repository.isInternetAvailable()
    }
}
