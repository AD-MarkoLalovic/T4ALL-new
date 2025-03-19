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
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.mobility.enp.MyApplication
import com.mobility.enp.R
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_tags.LostTagResponse
import com.mobility.enp.data.model.api_tool_history.TimeSave
import com.mobility.enp.data.model.api_tool_history.complaint.ComplaintBody
import com.mobility.enp.data.model.api_tool_history.complaint.ObjectionBody
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import com.mobility.enp.data.model.api_tool_history.index.Tag
import com.mobility.enp.data.model.api_tool_history.listing.ToolHistoryListing
import com.mobility.enp.data.model.csv_table.CsvModel
import com.mobility.enp.data.model.pdf_table.CsvTable
import com.mobility.enp.data.repository.PassageHistoryRepository
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.network.Repository
import com.mobility.enp.services.MyFirebaseMessagingService.Companion.CHANNEL_ID
import com.mobility.enp.services.MyFirebaseMessagingService.Companion.NOTIFICATION_ID
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.view.CsvActivity
import com.mobility.enp.view.adapters.tool_history.main_screen.ToolHistoryListingAdapter
import com.mobility.enp.view.adapters.tool_history.result.HistoryResultAdapter
import com.mobility.enp.view.fragments.tool_history.ToolHistoryFilterFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private val _baseTagDataState = MutableStateFlow<SubmitResult<IndexData>>(SubmitResult.Loading)
    val baseTagDataState: StateFlow<SubmitResult<IndexData>> get() = _baseTagDataState

    private val _complaintObjectionState =
        MutableStateFlow<SubmitResult<LostTagResponse>>(SubmitResult.Loading)
    val complaintObjectionState: StateFlow<SubmitResult<LostTagResponse>> get() = _complaintObjectionState


    suspend fun getLanguage(): String {
        return withContext(Dispatchers.IO) {
            repository.getUserLanguage()
        }
    }


    fun setStateIndex(indexData: IndexData) { // from room
        _baseTagDataState.value = SubmitResult.Success(indexData)
    }

    private val _errorBody: MutableLiveData<ErrorBody> = MutableLiveData()
    val errorBody: LiveData<ErrorBody> get() = _errorBody

    private var countryCode: String? = null


    fun setCountryCode(countryCode: String) {
        this.countryCode = countryCode
    }

    fun getCountryCode(): String {
        return countryCode ?: ""
    }

    var startDate = MutableLiveData<TimeSave>()
    var endDate = MutableLiveData<TimeSave>()
    private var userSelectedCalendarStart: Long? = null
    private var userSelectedCalendarEnd: Long? = null


    var allTagsSelected = false

    var selectedCurrency = ""

    fun nullDates() {
        startDate.value = TimeSave(null, null)
        endDate.value = TimeSave(null, null)
        userSelectedCalendarStart = null
        userSelectedCalendarEnd = null
    }


    private val itemsPerPage = 10

    fun getIndexData() {  // added token check here
        _baseTagDataState.value = SubmitResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getIndexData()
            if (result.isSuccess) {
                val body = result.getOrNull()
                if (body == null) {
                    _baseTagDataState.value = SubmitResult.Empty
                } else {
                    _baseTagDataState.value = SubmitResult.Success(body)
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

    fun getToolHistoryTransitResult(
        flow: MutableStateFlow<SubmitResult<ToolHistoryListing>>,
        tagSerialNumber: String,
        currentPage: Int,
        dateFrom: String,
        dateTo: String
    ) {
        flow.value = SubmitResult.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getToolHistoryTransitResult(
                tagSerialNumber,
                currentPage.toString(),
                itemsPerPage,
                dateFrom,
                dateTo,
                selectedCurrency
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

    fun getToolHistoryTransit(
        flow: MutableStateFlow<SubmitResult<ToolHistoryListing>>,
        tagSerialNumber: String,
        currentPage: Int
    ) {
        flow.value = SubmitResult.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getTagFill(tagSerialNumber, currentPage, itemsPerPage)
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


    fun getToolHistoryTransitPaginationUpdate(
        flow: MutableStateFlow<SubmitResult<ToolHistoryListing>>,
        tagSerialNumber: String,
        currentPage: Int
    ) {
        flow.value = SubmitResult.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getTagFill(tagSerialNumber, currentPage, itemsPerPage)
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
            repository.fetchPassageDataBySerial(tagSerialNumber)?.let {
                dataInterface.onOk(it)
            }
        }
    }

    fun fetchStoredData(
        dataInterface: HistoryResultAdapter.PassageDataInterface,
        tagSerialNumber: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.fetchPassageDataBySerial(tagSerialNumber)?.let {
                dataInterface.onOk(it)
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

    fun processCsvData(csvModel: CsvModel, nameExtra: String) {
        Log.d(TAG, "csv data: $csvModel")

        if (!csvModel.data?.csvContent.isNullOrEmpty()) {
            csvModel.data?.csvContent?.let { data ->
                viewModelScope.launch(Dispatchers.IO) {
                    saveCsvLocally(data, nameExtra) // <- csv excel export
                }
            }
        }
    }

    private suspend fun saveCsvLocally(encoded: String, nameExtra: String) = coroutineScope {
        try {
            // Decode the Base64 string
            val decodedBytes = Base64.decode(encoded, Base64.DEFAULT)
            val decodedString = String(decodedBytes)

            // Parse the data and format it as CSV
            val rows = decodedString.split("\n")
            val csvBuilder = StringBuilder()

            // Add a header if your data format is known
            csvBuilder.append("Bill number,Time of passage,Pay ramp,Price\n")

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
            } ?: Log.d(ToolHistoryFilterFragment.TAG, "Failed to create file URI in MediaStore.")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveBase64ToCSV(base64Data: String, nameExtra: String) {
        viewModelScope.launch {
            try {

                // Regular expression to match CSV fields with commas, allowing quoted fields to contain commas
                val regex = """"([^"]*)"|([^",]+)""".toRegex()

                // Decode the Base64 string
                val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                val decodedString = String(decodedBytes)

                val rows = decodedString.split("\n")
                val headers = listOf(
                    repository.fetchContext().getString(R.string.bill_number),
                    repository.fetchContext().getString(R.string.time_of_passage),
                    repository.fetchContext().getString(R.string.pay_ramp),
                    repository.fetchContext().getString(R.string.price)
                )


                val byteArrayOutputStream = ByteArrayOutputStream()
                val pdfWriter = PdfWriter(byteArrayOutputStream)
                val pdfDocument = PdfDocument(pdfWriter)
                val document = Document(pdfDocument)

                val table = Table(headers.size)

                headers.forEach { header ->
                    table.addCell(Cell().add(Paragraph(header).setBold()))
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


    suspend fun fetchIndexData(): IndexData {
        return withContext(Dispatchers.IO) {
            repository.getIndexDataRoom()
        }
    }


    private var _complaintResponseFiltered: MutableLiveData<LostTagResponse> = MutableLiveData()
    val complaintResponseFiltered: LiveData<LostTagResponse> get() = _complaintResponseFiltered

    private var _data: MutableLiveData<IndexData> = MutableLiveData<IndexData>()
    val data: LiveData<IndexData> get() = _data

    private val _csvData: MutableLiveData<CsvModel> = MutableLiveData()
    val csvData: LiveData<CsvModel> get() = _csvData

    var tagSerials: ArrayList<Tag> = ArrayList()
    var selectedTags: ArrayList<Tag> = ArrayList()

    private val database = DRoom.getRoomInstance(repository.fetchContext())


    suspend fun insertRoomToolHistoryIndexData(indexData: IndexData) {
        database.toolHistoryDao()?.deleteData()
        database.toolHistoryDao()?.insertData(indexData)
    }

    suspend fun insertPassageData(toolHistoryListing: ToolHistoryListing) {
        database.toolListingDao()?.insertData(toolHistoryListing)
    }

    suspend fun postComplaintFiltered(
        complaintBody: ComplaintBody,
        errorBody: MutableLiveData<ErrorBody>,
    ) {
        database.loginDao()?.fetchAllowedUsers()?.accessToken.let {
            Repository.postComplaint(it, errorBody, complaintBody, _complaintResponseFiltered)
        }
    }

    suspend fun postObjectionFiltered(
        objectionBody: ObjectionBody,
        errorBody: MutableLiveData<ErrorBody>,
    ) {
        database.loginDao()?.fetchAllowedUsers()?.accessToken.let {
            Repository.postObjection(it, errorBody, objectionBody, _complaintResponseFiltered)
        }
    }

    fun getToolHistoryTransitResultPagination(
        flow: MutableStateFlow<SubmitResult<ToolHistoryListing>>,
        tagSerialNumber: String,
        requestedPage: Int,
    ) {
        flow.value = SubmitResult.Loading

        val dateFrom = startDate.value?.formattedTime?.replace("/", ".")
        val dateTo = endDate.value?.formattedTime?.replace("/", ".")

        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getToolHistoryTransitResultPagination(
                tagSerialNumber,
                requestedPage.toString(),
                itemsPerPage,
                dateFrom ?: "",
                dateTo ?: "",
                selectedCurrency
            )
            val body = result.getOrNull()
            body?.let { data ->

                if (result.isSuccess) {
                    flow.value = SubmitResult.Success(body)
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

    }


    fun showDatePicker(fromDate: Boolean, context: Context) {
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

            val locale = when (val lang = getLanguage()) {
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

    private fun convertLongToDateString(time: Long): TimeSave {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val date = Date(time)
        val formDate = sdf.format(date)
        return TimeSave(formDate, date)
    }

    suspend fun getCsvData(context: Context) = coroutineScope {
        if (startDate.value?.inDateForm?.time != null && endDate.value?.inDateForm?.time != null) {

            if (endDate.value?.inDateForm!!.before(startDate.value?.inDateForm!!)) {
                _errorBody.postValue(ErrorBody(200, context.getString(R.string.end_date_check)))
            } else {
                try {
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
                            tagSerials[0].serialNumber ?: ""
                        }

                        Log.d(TAG, "getCsvData: $tagSerial")

                        database.loginDao()?.fetchAllowedUsers()?.accessToken?.let { token ->
                            Repository.getCsvData(
                                token,
                                repository.fetchContext(),
                                tagSerial,
                                dateStartApi,
                                dateEndApi,
                                selectedCurrency,
                                _errorBody,
                                _csvData
                            )
                        }

                    } else {
                        _errorBody.postValue(
                            ErrorBody(
                                200, context.getString(R.string.please_select_one_tag)
                            )
                        )
                    }

                } catch (e: Exception) {
                    Log.d(TAG, "getCsvData: ${e.message} ${e.cause}")
                    _errorBody.postValue(
                        ErrorBody(
                            200, context.getString(R.string.formatting_error)
                        )
                    )
                }

            }

        } else {
            _errorBody.postValue(ErrorBody(200, context.getString(R.string.please_select_dates)))
        }
    }

    suspend fun fetchCsvData(): ByteArray? {
        return withContext(Dispatchers.IO) {
            database.csvTableDao().fetchData().data
        }
    }

    fun internetAvailable(): Boolean {
        return repository.isInternetAvailable()
    }
}