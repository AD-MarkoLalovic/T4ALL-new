package com.mobility.enp.viewmodel

import android.Manifest
import android.app.Application
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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
import com.mobility.enp.R
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_tags.LostTagResponse
import com.mobility.enp.data.model.api_tool_history.TimeSave
import com.mobility.enp.data.model.api_tool_history.listing.ToolHistoryListing
import com.mobility.enp.data.model.api_tool_history.complaint.ComplaintBody
import com.mobility.enp.data.model.api_tool_history.complaint.ObjectionBody
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import com.mobility.enp.data.model.api_tool_history.index.Tag
import com.mobility.enp.data.model.csv_table.CsvModel
import com.mobility.enp.data.model.pdf_table.CsvTable
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.network.Repository
import com.mobility.enp.view.CsvActivity
import com.mobility.enp.view.adapters.tool_history.main_screen.ToolHistoryListingAdapter
import com.mobility.enp.view.adapters.tool_history.result.HistoryResultAdapter
import com.mobility.enp.view.fragments.tool_history.ToolHistoryFilterFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


class PassageHistoryViewModel(private var application: Application) :
    AndroidViewModel(application) {

    private var _complaintResponseFiltered: MutableLiveData<LostTagResponse> = MutableLiveData()
    val complaintResponseFiltered: LiveData<LostTagResponse> get() = _complaintResponseFiltered
    private val _errorBody: MutableLiveData<ErrorBody> = MutableLiveData()
    val errorBody: LiveData<ErrorBody> get() = _errorBody
    private var countryCode: String? = null

    private var _data: MutableLiveData<IndexData> = MutableLiveData<IndexData>()
    val data: LiveData<IndexData> get() = _data

    private val _csvData: MutableLiveData<CsvModel> = MutableLiveData()
    val csvData: LiveData<CsvModel> get() = _csvData

    private val itemsPerPage = 10

    var tagSerials: ArrayList<Tag> = ArrayList()
    var selectedTags: ArrayList<Tag> = ArrayList()

    var startDate = MutableLiveData<TimeSave>()
    var endDate = MutableLiveData<TimeSave>()
    private var userSelectedCalendarStart: Long? = null
    private var userSelectedCalendarEnd: Long? = null

    fun setCountryCode(countryCode: String) {
        this.countryCode = countryCode
    }

    fun getCountryCode(): String {
        return countryCode ?: ""
    }

    var allTagsSelected = false

    var selectedCurrency = ""

    fun nullDates() {
        startDate.value = TimeSave(null, null)
        endDate.value = TimeSave(null, null)
        userSelectedCalendarStart = null
        userSelectedCalendarEnd = null
    }

    companion object {
        const val TAG = "VmPassage"
        const val CHANNEL_ID = "search_passage"
        const val NOTIFICATION_ID = 4
    }

    private val database = DRoom.getRoomInstance(application)


    suspend fun insertRoomToolHistoryIndexData(indexData: IndexData) {
        database.toolHistoryDao()?.deleteData()
        database.toolHistoryDao()?.insertData(indexData)
    }

    suspend fun fetchIndexData(): IndexData? {
        return withContext(Dispatchers.IO) {
            database.toolHistoryDao()?.fetchData()
        }
    }

    suspend fun insertPassageData(toolHistoryListing: ToolHistoryListing) {
        database.toolListingDao()?.insertData(toolHistoryListing)
    }

    suspend fun getToolHistoryIndex(
        context: Context,
        isInternetAvailable: MutableLiveData<Boolean>
    ) {

        if (Repository.isNetworkAvailable(context)) {
            database.loginDao()?.fetchAllowedUsers()?.accessToken?.let {
                Repository.getToolHistoryIndex(_data, it, _errorBody)
            } ?: run {
                Log.d(TAG, "database initialization issue: ")
            }
        } else {
            isInternetAvailable.postValue(false)
        }

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


    //getToolHistoryListingMutable

    suspend fun getToolHistoryListingMutableTimeFiltered(
        data: MutableLiveData<ToolHistoryListing>,
        errorBody: MutableLiveData<ErrorBody>,
        tagSerialNumber: String,
        requestedPage: Int
    ) {
        database.loginDao()?.fetchAllowedUsers()?.accessToken?.let {
            val dateFrom = startDate.value?.formattedTime?.replace("/", ".")
            val dateTo = endDate.value?.formattedTime?.replace("/", ".")
            Repository.getToolHistoryListingMutableTimeFiltered(
                data,
                errorBody,
                it,
                tagSerialNumber,
                requestedPage,
                itemsPerPage,
                getApplication(),
                dateFrom!!,
                dateTo!!,
                selectedCurrency
            )
        }
    }


    suspend fun getToolHistoryTransitResult(
        dataInterface: HistoryResultAdapter.PassageDataInterface,
        tagSerialNumber: String,
        currentPage: Int,
        dateFrom: String,
        dateTo: String
    ) {
        database.loginDao()?.fetchAllowedUsers()?.accessToken?.let {
            Repository.getToolHistoryListingResult(
                dataInterface,
                it,
                tagSerialNumber,
                currentPage,
                itemsPerPage,
                dateFrom,
                dateTo,
                getApplication(),
                selectedCurrency
            )
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

            val langContext =
                getLocale()  // dont delete this it gives context to date picker even if the variable is not used.

            val datePicker = MaterialDatePicker.Builder.datePicker()

                .setTitleText(context.getString(R.string.select_date))
                .setSelection(selectedDate)
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

    private suspend fun getLocale(): Context? {
        return withContext(Dispatchers.IO) {
            val languageKey = database.languageDao().fetchAllowedUsers()?.userLanguage
            languageKey?.let { key ->
                val locale: Locale
                if (key == "cyr" || key.isEmpty()) {
                    locale = Locale("SR")
                } else if (key == "sr" || key == "cnr") {
                    locale =
                        Locale.Builder().setLanguage("sr").setRegion("RS").setScript("Latn").build()
                } else {
                    locale = Locale(key)
                }
                Locale.setDefault(locale)
                val config = application.resources.configuration
                config.setLocale(locale)
                application.createConfigurationContext(config)
            }
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
                                application,
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

    fun processCsvData(csvModel: CsvModel) {
        Log.d(TAG, "csv data: $csvModel")

        if (!csvModel.data?.csvContent.isNullOrEmpty()) {
            csvModel.data?.csvContent?.let { data ->
                val nameExtra = UUID.randomUUID().toString().substring(0, 8)

                viewModelScope.launch(Dispatchers.IO) {
                    saveCsvLocally(data, nameExtra) // <- csv excel export
                    saveBase64ToCSV(
                        data, nameExtra
                    ) // <- converts csv to pdf saves locally and in room byte array
                }

            }
        } else {
            _errorBody.postValue(ErrorBody(200, "No export data received"))
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
            val uri = application.contentResolver.insert(
                MediaStore.Files.getContentUri("external"), contentValues
            )

            uri?.let { fileUri ->
                application.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
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

    private suspend fun saveBase64ToCSV(base64Data: String, nameExtra: String) = coroutineScope {
        try {

            // Regular expression to match CSV fields with commas, allowing quoted fields to contain commas
            val regex = """"([^"]*)"|([^",]+)""".toRegex()

            // Decode the Base64 string
            val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
            val decodedString = String(decodedBytes)

            val rows = decodedString.split("\n")
            val headers = listOf(
                application.getString(R.string.bill_number),
                application.getString(R.string.time_of_passage),
                application.getString(R.string.pay_ramp),
                application.getString(R.string.price)
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
            database.csvTableDao().deleteData()
            database.csvTableDao().upsertData(CsvTable(0, pdfData))

            // Save CSV to shared storage using MediaStore
            val fileName = "export-$nameExtra.csv"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH, "Documents/"
                ) // Save in the Documents folder
            }

            val uri = application.contentResolver.insert(
                MediaStore.Files.getContentUri("external"), contentValues
            )

            uri?.let { fileUri ->
                application.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                    outputStream.write(pdfData)
                    outputStream.flush()

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        postNotification()
                    } else {
                        runPermissionCheck()
                    }

                    Log.d(
                        ToolHistoryFilterFragment.TAG,
                        "PDF file saved successfully in Documents folder."
                    )
                } ?: Log.d(ToolHistoryFilterFragment.TAG, "Failed to open OutputStream.")
            } ?: Log.d(ToolHistoryFilterFragment.TAG, "Failed to create file URI in MediaStore.")

        } catch (e: Exception) {
            e.printStackTrace()
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

        val intent = Intent(application, CsvActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(application, 100, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationManager = application.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(
            application, CHANNEL_ID
        ).setSmallIcon(R.drawable.splash_logo)
            .setContentTitle(application.getString(R.string.export)).setContentIntent(pendingIntent)
            .setContentText(application.getString(R.string.csv_saved)).setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(
                application, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(application).notify(NOTIFICATION_ID, builder.build())

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun runPermissionCheck() {
        Dexter.withContext(application)
            .withPermission(Manifest.permission.POST_NOTIFICATIONS)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    postNotification()
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(application, R.string.csv_saved, Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?, p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }

            }).check()
    }

    suspend fun fetchCsvData(): ByteArray? {
        return withContext(Dispatchers.IO) {
            database.csvTableDao().fetchData().data
        }
    }

}

