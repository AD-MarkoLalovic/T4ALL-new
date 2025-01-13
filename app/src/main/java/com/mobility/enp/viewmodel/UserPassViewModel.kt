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
import com.mobility.enp.data.model.api_tool_history.complaint.ComplaintBody
import com.mobility.enp.data.model.api_tool_history.complaint.ObjectionBody
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import com.mobility.enp.data.model.api_tool_history.listing.ToolHistoryListing
import com.mobility.enp.data.model.csv_table.CsvModel
import com.mobility.enp.data.model.pdf_table.CsvTable
import com.mobility.enp.data.repository.PassageHistoryRepository
import com.mobility.enp.network.Repository
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.view.CsvActivity
import com.mobility.enp.view.adapters.tool_history.main_screen.ToolHistoryListingAdapter
import com.mobility.enp.view.fragments.tool_history.ToolHistoryFilterFragment
import com.mobility.enp.viewmodel.PassageHistoryViewModel.Companion
import com.mobility.enp.viewmodel.PassageHistoryViewModel.Companion.CHANNEL_ID
import com.mobility.enp.viewmodel.PassageHistoryViewModel.Companion.NOTIFICATION_ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

class UserPassViewModel(private val repository: PassageHistoryRepository) : ViewModel() {

    private val _baseTagDataState = MutableStateFlow<SubmitResult<IndexData>>(SubmitResult.Loading)
    val baseTagDataState: StateFlow<SubmitResult<IndexData>> get() = _baseTagDataState

    private val _complaintObjectionState =
        MutableStateFlow<SubmitResult<LostTagResponse>>(SubmitResult.Loading)
    val complaintObjectionState: StateFlow<SubmitResult<LostTagResponse>> get() = _complaintObjectionState

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


    private val itemsPerPage = 10

    fun getIndexData() {
        _baseTagDataState.value = SubmitResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getIndexData()
            val body = result.getOrNull()
            body?.let { data ->
                if (result.isSuccess) {
                    _baseTagDataState.value = SubmitResult.Success(data)
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
                            _baseTagDataState.value =
                                SubmitResult.FailureApiError(error.errorResponse.message ?: "")
                            Log.d(TAG, "api error ${error.errorResponse.message}")
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    fun postComplaint(complaintBody: ComplaintBody) {
        _complaintObjectionState.value = SubmitResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.postComplaint(complaintBody)
            val body = result.getOrNull()
            body?.let { data ->
                if (result.isSuccess) {
                    _complaintObjectionState.value = SubmitResult.Success(data)
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
                            _baseTagDataState.value =
                                SubmitResult.FailureApiError(error.errorResponse.message ?: "")
                            Log.d(TAG, "api error ${error.errorResponse.message}")
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    fun postObjection(objectionBody: ObjectionBody) {
        _complaintObjectionState.value = SubmitResult.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.postObjection(objectionBody)
            val body = result.getOrNull()
            body?.let { data ->
                if (result.isSuccess) {
                    _complaintObjectionState.value = SubmitResult.Success(data)
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
                            _baseTagDataState.value =
                                SubmitResult.FailureApiError(error.errorResponse.message ?: "")
                            Log.d(TAG, "api error ${error.errorResponse.message}")
                        }

                        else -> {}
                    }
                }
            }
        }
    }


    suspend fun getToolHistoryTransit(
        dataInterface: ToolHistoryListingAdapter.PassageDataInterface,
        context: Context,
        tagSerialNumber: String,
        currentPage: Int
    ) {
        if (Repository.isNetworkAvailable(context)) {
            repository.getToken()?.let { token ->
                Repository.getToolHistoryListing(
                    dataInterface,
                    token,
                    tagSerialNumber,
                    currentPage,
                    itemsPerPage,
                    repository.fetchContext()
                )
            }
        } else {
            viewModelScope.launch {// fetch stored data send back to adapter with interface
                repository.fetchPassageDataBySerial(tagSerialNumber)?.let {
                    dataInterface.onOk(it)
                }
            }
        }
    }

    suspend fun getToolHistoryListingMutable(
        data: MutableLiveData<ToolHistoryListing>, tagSerialNumber: String, requestedPage: Int
    ) {
        repository.getToken()?.let {
            Repository.getToolHistoryListingMutable(
                data,
                _errorBody,
                it,
                tagSerialNumber,
                requestedPage,
                itemsPerPage,
                repository.fetchContext()
            )
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun runPermissionCheck() {
        Dexter.withContext(repository.fetchContext())
            .withPermission(Manifest.permission.POST_NOTIFICATIONS)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    postNotification()
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(
                        repository.fetchContext(),
                        R.string.csv_saved,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?, p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }

            }).check()
    }


    fun processCsvData(csvModel: CsvModel) {
        Log.d(PassageHistoryViewModel.TAG, "csv data: $csvModel")

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
            Toast.makeText(repository.fetchContext(), repository.fetchContext().resources.getString(R.string.no_passage_data), Toast.LENGTH_SHORT).show()
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
            val uri = repository.fetchContext().contentResolver.insert(
                MediaStore.Files.getContentUri("external"), contentValues
            )

            uri?.let { fileUri ->
                repository.fetchContext().contentResolver.openOutputStream(fileUri)?.use { outputStream ->
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
                repository.fetchContext().contentResolver.openOutputStream(fileUri)?.use { outputStream ->
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


    suspend fun fetchIndexData(): IndexData {
        return withContext(Dispatchers.IO) {
            repository.getIndexDataRoom()
        }
    }

    companion object {
        const val TAG = "PassViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepository = (this[APPLICATION_KEY] as MyApplication).passageHistoryRepository
                UserPassViewModel(
                    repository = myRepository
                )
            }
        }
    }


}