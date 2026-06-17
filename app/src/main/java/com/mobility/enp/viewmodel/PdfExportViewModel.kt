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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.mobility.enp.MyApplication
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.TimeSave
import com.mobility.enp.data.model.api_tool_history.index.Tag
import com.mobility.enp.data.model.pdf_table.CsvTable
import com.mobility.enp.data.model.pdf_table.FilterPdf
import com.mobility.enp.data.repository.PassageHistoryRepository
import com.mobility.enp.services.MyFirebaseMessagingService.Companion.CHANNEL_ID
import com.mobility.enp.services.MyFirebaseMessagingService.Companion.NOTIFICATION_ID
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.view.CsvActivity
import com.mobility.enp.view.PdfHistoryActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfExportViewModel(
    private val repository: PassageHistoryRepository,
) : ViewModel() {

    companion object {
        const val TAG = "PdfExportViewModel"
        const val TOKEN = "API_TOKEN"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val repository = (this[APPLICATION_KEY] as MyApplication).passageHistoryRepository
                PdfExportViewModel(
                    repository = repository,
                )
            }
        }
    }

    private val _pdfTable = MutableStateFlow<SubmitResult<ByteArray>>(SubmitResult.Empty)
    val pdfTable: StateFlow<SubmitResult<ByteArray>> get() = _pdfTable

    fun nullPdfState() {
        _pdfTable.value = SubmitResult.Empty
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
                                TAG,
                                "PDF file saved successfully in Documents folder."
                            )
                        } ?: Log.d(TAG, "Failed to open OutputStream.")
                } ?: Log.d(
                    TAG, "Failed to create file URI in MediaStore."
                )

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getPDFData(
        context: Context,
        startDate: TimeSave?,
        endDate: TimeSave?,
        userSelectedCalendarStart: Long?,
        userSelectedCalendarEnd: Long?,
        selectedTags: List<Tag>,
        userSelectedTags: List<Tag>,
        allTagsSelected: Boolean,
        selectedCountry: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _pdfTable.value = SubmitResult.Empty
            repository.deletePdfExportData()
            if (startDate?.inDateForm?.time != null && endDate?.inDateForm?.time != null) {
                if (endDate.inDateForm.before(startDate.inDateForm)) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context, context.getString(R.string.end_date_check), Toast.LENGTH_SHORT
                        ).show()
                    }
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

                        if (selectedTags.isNotEmpty() && userSelectedTags.size == 1 || allTagsSelected) {

                            val tagSerial = if (allTagsSelected) {
                                ""
                            } else {
                                userSelectedTags.first().serialNumber ?: ""
                            }

                            val result = repository.getPDFTableData(
                                tagSerial, dateStartApi, dateEndApi, selectedCountry
                            )

                            val body = result.getOrNull()
                            body?.let { data ->

                                if (result.isSuccess) {
                                    repository.upsertPdfExportData(FilterPdf(0, "my_pdf", data))
                                    _pdfTable.value = SubmitResult.Success(data)
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
                                                        error.errorResponse.code,
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
                        Log.d(TAG, "getPDFData: ${e.message} ${e.cause}")
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

    suspend fun fetchPDFData(): ByteArray? {
        return repository.fetchedStoredPDFData()
    }
}
