package com.mobility.enp.viewmodel

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.util.Base64
import android.util.Log
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
import com.mobility.enp.MyApplication
import com.mobility.enp.R
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_my_invoices.BillsDetailsResponse
import com.mobility.enp.data.model.api_my_invoices.refactor.MyInvoicesResponse
import com.mobility.enp.data.model.pdf_table.PdfTable
import com.mobility.enp.data.repository.BillsRepository
import com.mobility.enp.network.Repository
import com.mobility.enp.services.MyFirebaseMessagingService
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.view.PdfViewActivity
import com.mobility.enp.view.adapters.my_invoices_adapters.BillsDetailsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class MyInvoicesViewModel(private val repository: BillsRepository) : ViewModel() {

    companion object {
        const val TAG = "BillViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myRepository = (this[APPLICATION_KEY] as MyApplication).billsRepository
                MyInvoicesViewModel(
                    repository = myRepository
                )
            }
        }
    }

    private val perPage = 10


    private val _myInvoices = MutableStateFlow<SubmitResult<MyInvoicesResponse>>(SubmitResult.Empty)
    val myInvoices: StateFlow<SubmitResult<MyInvoicesResponse>> get() = _myInvoices

    private val _checkNetDownload = MutableLiveData<Boolean>()
    val checkNetDownload: LiveData<Boolean> = _checkNetDownload

    private val _billPad = MutableLiveData<Boolean>()
    val billPad: LiveData<Boolean> get() = _billPad

    private val _savedPdfData = MutableLiveData<ByteArray>()
    val pdfData: LiveData<ByteArray> get() = _savedPdfData

    private val _openDialogForNoPdfData = MutableLiveData<Boolean>()
    val openDialogForNoPdfData: LiveData<Boolean> get() = _openDialogForNoPdfData

    private val _checkNetMyInvoices = MutableLiveData<Boolean>()
    val checkNetMyInvoices: LiveData<Boolean> get() = _checkNetMyInvoices

    private var selectedCountry: String = ""

    fun setSelectedCountry(country: String) {
        this.selectedCountry = country
    }

    private suspend fun getUserToken(): String? {
        return withContext(Dispatchers.IO) {
            repository.getTokenTemp()
        }
    }

    fun isNetworkAvailable(): Boolean {
        return repository.isNetworkPresent()
    }

    fun fetchMonthlyInvoices() {
        _myInvoices.value = SubmitResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getInvoicesData(perPage, selectedCountry)
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data == null) {
                    _myInvoices.value = SubmitResult.Empty
                } else {
                    _myInvoices.value = SubmitResult.Success(data)
                }
            } else {
                when (val error = result.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.d(
                            UserPassViewModel.Companion.TAG,
                            "Error while fetching my invoices data"
                        )
                        _myInvoices.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _myInvoices.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(
                                    "API_TOKEN UserPassViewModel",
                                    "invalid token detected login out user"
                                )
                                _myInvoices.value =
                                    SubmitResult.InvalidApiToken(
                                        error.errorResponse.code,
                                        error.errorResponse.message ?: ""
                                    )
                            }

                            else -> {
                                _myInvoices.value =
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

    fun fetchMonthlyInvoicesPaging(
        page: Int,
        flow: MutableStateFlow<SubmitResult<MyInvoicesResponse>>
    ) {
        flow.value = SubmitResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getInvoicesDataPaging(page, perPage, selectedCountry)
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
                        Log.d(
                            UserPassViewModel.Companion.TAG,
                            "Error while fetching my invoices data"
                        )
                        flow.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        flow.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(
                                    "API_TOKEN UserPassViewModel",
                                    "invalid token detected login out user"
                                )
                                flow.value =
                                    SubmitResult.InvalidApiToken(
                                        error.errorResponse.code,
                                        error.errorResponse.message ?: ""
                                    )
                            }

                            else -> {
                                flow.value =
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

    fun setLocalData(bills: MyInvoicesResponse) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setLocalBillsData(bills)
        }
    }

    suspend fun checkBills() = withContext(Dispatchers.IO) {
        repository.fetchSavedBillsData()
    }

    fun fetchLocalData() {
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                repository.fetchSavedBillsData()
            }
            _myInvoices.value = SubmitResult.Success(data)
        }
    }

    fun fetchBillDetailsNew(
        flow: MutableStateFlow<SubmitResult<BillsDetailsResponse>>,
        yearMonth: String,
        currency: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getBillDetails(yearMonth, currency, perPage, selectedCountry)
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
                        Log.d(
                            UserPassViewModel.Companion.TAG,
                            "Error while fetching bill details"
                        )
                        flow.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        flow.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(
                                    "API_TOKEN UserPassViewModel",
                                    "invalid token detected login out user"
                                )
                                flow.value =
                                    SubmitResult.InvalidApiToken(
                                        error.errorResponse.code,
                                        error.errorResponse.message ?: ""
                                    )
                            }

                            else -> {
                                flow.value =
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


    fun fetchBillDetailsNewPaging(
        flow: MutableStateFlow<SubmitResult<BillsDetailsResponse>>,
        yearMonth: String,
        currency: String,
        page: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result =
                repository.getBillDetailsPaging(yearMonth, currency, perPage, selectedCountry, page)
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
                        Log.d(
                            UserPassViewModel.Companion.TAG,
                            "Error while fetching bill paging detals"
                        )
                        flow.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        flow.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(
                                    "API_TOKEN UserPassViewModel",
                                    "invalid token detected login out user"
                                )
                                flow.value =
                                    SubmitResult.InvalidApiToken(
                                        error.errorResponse.code,
                                        error.errorResponse.message ?: ""
                                    )
                            }

                            else -> {
                                flow.value =
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

    fun payBill(billId: String, errorBody: MutableLiveData<ErrorBody>) {
        if (isNetworkAvailable()) {
            viewModelScope.launch {
                val userToken = getUserToken()
                userToken?.let { token ->
                    Repository.postPayBill(
                        token, billId, _billPad, errorBody
                    )
                }
            }
        } else {
            _checkNetMyInvoices.postValue(false)
        }
    }

    fun downloadPdfBill(
        adapterInterface: BillsDetailsAdapter.DownloadBillsDetails,
        billId: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result =
                repository.downloadPdfData(billId)
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data == null) {
                    adapterInterface.onFailed()
                } else {
                    withContext(Dispatchers.Main) {
                        adapterInterface.onOK(data)
                    }
                }
            } else {
                when (val error = result.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.d(
                            UserPassViewModel.Companion.TAG,
                            "Error while fetching bill paging detals"
                        )
                        _myInvoices.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _myInvoices.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(
                                    "API_TOKEN UserPassViewModel",
                                    "invalid token detected login out user"
                                )
                                _myInvoices.value =
                                    SubmitResult.InvalidApiToken(
                                        error.errorResponse.code,
                                        error.errorResponse.message ?: ""
                                    )
                            }

                            else -> {
                                _myInvoices.value =
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun savePdfToDrive(
        base64EncodedData: String,
        fileName: String,
        context: Context,
        isListingOfPassages: Boolean
    ) {
        val decodedData = Base64.decode(base64EncodedData, Base64.DEFAULT)

        // Postavljanje poruke u zavisnosti od tipa fajla
        val contentText = if (isListingOfPassages) {
            context.getString(R.string.listing_of_passages_downloaded_successfully)
        } else {
            context.getString(R.string.bill_downloaded_successfully)
        }

        try {
            val futureStudioIconFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )


            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                val fileSize: Long = decodedData.size.toLong()
                var fileSizeDownloaded: Long = 0

                inputStream = ByteArrayInputStream(decodedData)

                viewModelScope.launch(Dispatchers.IO) {
                    repository.savePdfData(decodedData)
                }

                outputStream = FileOutputStream(futureStudioIconFile)

                val fileReader = ByteArray(4096)
                while (true) {
                    val read = inputStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()
                    Log.d("File Download: ", "$fileSizeDownloaded of $fileSize")
                }

                val intent = Intent(context, PdfViewActivity::class.java)
                val pendingIntent =
                    PendingIntent.getActivity(
                        context,
                        100,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )

                val channel = NotificationChannel(
                    MyFirebaseMessagingService.CHANNEL_ID,
                    "Tool4all",
                    NotificationManager.IMPORTANCE_HIGH
                )
                channel.description = "Tool4all"
                channel.enableLights(true)
                channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                channel.lightColor = Color.BLUE

                val notificationManager =
                    context.getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)

                val builder = NotificationCompat.Builder(
                    context, MyFirebaseMessagingService.CHANNEL_ID
                ).setSmallIcon(R.drawable.select_country_icon)
                    .setContentTitle(context.getString(R.string.file_downloaded))
                    .setContentText(contentText)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)

                if (ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                NotificationManagerCompat.from(context)
                    .notify(MyFirebaseMessagingService.NOTIFICATION_ID, builder.build())

                outputStream.flush()


            } catch (e: IOException) {
                e.printStackTrace()

            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    inputStream?.close()
                    outputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    suspend fun loadPdf() {
        val data: ByteArray? = fetchPdf().data
        if (data != null && data.isNotEmpty()) {
            _savedPdfData.postValue(data)
        } else {
            _openDialogForNoPdfData.postValue(true)
        }
    }

    private suspend fun fetchPdf(): PdfTable {
        return withContext(Dispatchers.IO) {
            repository.getPdfTable()
        }
    }

    fun downloadPassageData(
        adapterInterface: BillsDetailsAdapter.DownloadBillsDetails,
        billId: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result =
                repository.downloadPassageData(billId)
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data == null) {
                    adapterInterface.onFailed()
                } else {
                    withContext(Dispatchers.Main) {
                        adapterInterface.onOK(data)
                    }
                }
            } else {
                when (val error = result.exceptionOrNull()) {
                    is NetworkError.ServerError -> {
                        Log.d(
                            UserPassViewModel.Companion.TAG,
                            "Error while fetching bill paging detals"
                        )
                        _myInvoices.value = SubmitResult.FailureServerError
                    }

                    is NetworkError.NoConnection -> {
                        _myInvoices.value = SubmitResult.FailureNoConnection
                    }

                    is NetworkError.ApiError -> {
                        when (error.errorResponse.code) {
                            401, 405 -> {
                                Log.d(
                                    "API_TOKEN UserPassViewModel",
                                    "invalid token detected login out user"
                                )
                                _myInvoices.value =
                                    SubmitResult.InvalidApiToken(
                                        error.errorResponse.code,
                                        error.errorResponse.message ?: ""
                                    )
                            }

                            else -> {
                                _myInvoices.value =
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

}