package com.mobility.enp.viewmodel

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobility.enp.MyApplication
import com.mobility.enp.R
import com.mobility.enp.data.model.csv_table.CsvModel
import com.mobility.enp.data.repository.PassageHistoryRepository
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CsvExportViewModel(
    private val repository: PassageHistoryRepository
) : ViewModel() {

    private val _csvTable = MutableStateFlow<SubmitResult<CsvModel>>(SubmitResult.Empty)
    val csvTable: StateFlow<SubmitResult<CsvModel>> get() = _csvTable

    fun nullFlowState() {
        _csvTable.value = SubmitResult.Empty
    }

    fun getCsvData(
        context: Context,
        startDateValue: Date?,
        endDateValue: Date?,
        userSelectedCalendarStart: Long?,
        userSelectedCalendarEnd: Long?,
        selectedTagsNotEmpty: Boolean,
        userSelectedTagsSize: Int,
        firstUserSelectedTagSerialNumber: String?,
        allTagsSelected: Boolean,
        selectedCountry: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _csvTable.value = SubmitResult.Empty
            if (startDateValue != null && endDateValue != null) {
                if (endDateValue.before(startDateValue)) {
                    launch(Dispatchers.Main) {
                        Toast.makeText(
                            context, context.getString(R.string.end_date_check), Toast.LENGTH_SHORT
                        ).show()
                    }
                    _csvTable.value = SubmitResult.Empty
                } else {
                    try {
                        _csvTable.value = SubmitResult.Loading
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

                        val dateStart = Date(userSelectedCalendarStart ?: 0)
                        val dateEnd = Date(userSelectedCalendarEnd ?: 0)

                        val dateStartApi = sdf.format(dateStart)
                        val dateEndApi = sdf.format(dateEnd)

                        if (selectedTagsNotEmpty && userSelectedTagsSize == 1 || allTagsSelected) {

                            val tagSerial = if (allTagsSelected) {
                                ""
                            } else {
                                firstUserSelectedTagSerialNumber ?: ""
                            }

                            val result = repository.getCsvTableData(
                                tagSerial, dateStartApi, dateEndApi, selectedCountry
                            )

                            val body = result.getOrNull()
                            if (result.isSuccess && body != null) {
                                _csvTable.value = SubmitResult.Success(body)
                            } else {
                                handleError(result.exceptionOrNull())
                            }

                        } else {
                            _csvTable.value = SubmitResult.FailureApiError(
                                ContextCompat.getString(
                                    context, R.string.please_select_one_tag
                                )
                            )
                        }
                    } catch (e: Exception) {
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

    private fun handleError(error: Throwable?) {
        when (error) {
            is NetworkError.ServerError -> {
                _csvTable.value = SubmitResult.FailureServerError
            }

            is NetworkError.NoConnection -> {
                _csvTable.value = SubmitResult.FailureNoConnection
            }

            is NetworkError.ApiError -> {
                when (error.errorResponse.code) {
                    401, 405 -> {
                        _csvTable.value = SubmitResult.InvalidApiToken(
                            error.errorResponse.code,
                            error.errorResponse.message ?: ""
                        )
                    }

                    else -> {
                        _csvTable.value = SubmitResult.FailureApiError(
                            error.errorResponse.message ?: ""
                        )
                    }
                }
            }
            else -> {
                _csvTable.value = SubmitResult.FailureServerError
            }
        }
    }

    fun processCsvData(csvModel: CsvModel, nameExtra: String, context: Context) {
        if (!csvModel.data?.csvContent.isNullOrEmpty()) {
            csvModel.data?.csvContent?.let { data ->
                viewModelScope.launch(Dispatchers.IO) {
                    saveCsvLocally(data, nameExtra, context)
                }
            }
        }
    }

    private suspend fun saveCsvLocally(encoded: String, nameExtra: String, context: Context) =
        coroutineScope {
            try {
                val decodedBytes = Base64.decode(encoded, Base64.DEFAULT)
                val decodedString = String(decodedBytes)
                val rows = decodedString.split("\n")
                val csvBuilder = StringBuilder()

                val billNumber = ContextCompat.getString(context, R.string.bill_number)
                val price = ContextCompat.getString(context, R.string.price)
                val payRamp = ContextCompat.getString(context, R.string.pay_ramp)
                val timeOfPassage = ContextCompat.getString(context, R.string.time_of_passage)

                val titleHeader =
                    StringBuilder().append(billNumber).append(",").append(timeOfPassage).append(",")
                        .append(payRamp).append(",").append(price).append(",").append("\n")

                csvBuilder.append(titleHeader.toString())

                for (row in rows) {
                    csvBuilder.append(row).append("\n")
                }

                val fileName = "export-$nameExtra.csv"
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/")
                }

                val uri = repository.fetchContext().contentResolver.insert(
                    MediaStore.Files.getContentUri("external"), contentValues
                )

                uri?.let { fileUri ->
                    repository.fetchContext().contentResolver.openOutputStream(fileUri)
                        ?.use { outputStream ->
                            outputStream.write(csvBuilder.toString().toByteArray())
                            outputStream.flush()
                        }
                }
            } catch (e: Exception) {
                Log.e("CsvExportViewModel", "Error saving CSV", e)
            }
        }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myApplication = this[APPLICATION_KEY] as MyApplication
                val myRepository = myApplication.passageHistoryRepository
                CsvExportViewModel(
                    repository = myRepository
                )
            }
        }
    }
}
