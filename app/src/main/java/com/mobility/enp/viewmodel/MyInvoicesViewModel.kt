package com.mobility.enp.viewmodel

import android.Manifest
import android.app.Application
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
import com.mobility.enp.view.PdfViewActivity
import com.mobility.enp.view.adapters.my_invoices_adapters.BillsDetailsAdapter
import com.mobility.enp.view.adapters.my_invoices_adapters.MonthlyBillsAdapter
import kotlinx.coroutines.Dispatchers
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

    private val _monthlyInvoicesList = MutableLiveData<MyInvoicesResponse>()
    val monthlyInvoicesList: LiveData<MyInvoicesResponse?> get() = _monthlyInvoicesList

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

    fun fetchMonthlyInvoices(errorBody: MutableLiveData<ErrorBody>, context: Context) {
        if (isNetworkAvailable()) {
            _checkNetMyInvoices.value = true
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    val userToken = getUserToken()
                    userToken?.let { token ->
                        Repository.getInvoices(
                            _monthlyInvoicesList,
                            token,
                            errorBody,
                            context,
                            perPage,
                            selectedCountry
                        )
                    }
                }
            }
        } else {
            _checkNetMyInvoices.value = false
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
            _monthlyInvoicesList.value = data
        }
    }

    fun fetchMonthlyInvoicesPaging(
        errorBody: MutableLiveData<ErrorBody>,
        data: MutableLiveData<MyInvoicesResponse>,
        nextPage: Int, context: Context
    ) {
        if (isNetworkAvailable()) {
            viewModelScope.launch {
                val userToken = getUserToken()
                userToken?.let { token ->
                    Repository.getInvoicesPaging(
                        data,
                        token,
                        errorBody,
                        context,
                        nextPage,
                        perPage,
                        selectedCountry
                    )
                }
            }
        } else {
            _checkNetMyInvoices.postValue(false)
        }
    }

    fun fetchBillDetailsNew(
        data: MonthlyBillsAdapter.FetchBillsDetails,
        yearMonth: String,
        currency: String,
        error: MutableLiveData<ErrorBody>, context: Context
    ) {
        if (isNetworkAvailable()) {
            viewModelScope.launch {
                val userToken = getUserToken()
                userToken?.let { token ->
                    Repository.getBillsDetails(
                        data,
                        token,
                        yearMonth,
                        currency,
                        perPage,
                        error,
                        context,
                        selectedCountry
                    )
                }
            }
        } else {
            _checkNetMyInvoices.postValue(false)
        }
    }

    fun fetchBillDetailsPaging(
        data: MutableLiveData<BillsDetailsResponse>,
        yearMonth: String,
        currency: String,
        page: Int,
        error: MutableLiveData<ErrorBody>, context: Context
    ) {
        if (isNetworkAvailable()) {
            viewModelScope.launch {
                val userToken = getUserToken()
                userToken?.let { token ->
                    Repository.getBillsDetailsPaging(
                        data,
                        token,
                        yearMonth,
                        currency,
                        page,
                        perPage,
                        error,
                        context,
                        selectedCountry
                    )
                }
            }
        } else {
            _checkNetMyInvoices.postValue(false)
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

    fun downloadBill(
        data: BillsDetailsAdapter.DownloadBillsDetails,
        billId: String,
        errorBody: MutableLiveData<ErrorBody>
    ) {
        if (isNetworkAvailable()) {
            _checkNetDownload.value = true
            viewModelScope.launch {
                val userToken = getUserToken()
                userToken?.let { token ->
                    Repository.getBillDetailsPdf(
                        data, token, billId, errorBody
                    )
                }
            }
        } else {
            data.onFailed()
            _checkNetDownload.value = false
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
                    PendingIntent.getActivity(context, 100, intent, PendingIntent.FLAG_IMMUTABLE)

                val channel = NotificationChannel(
                    MyFirebaseMessagingService.CHANNEL_ID,
                    "Tool4all",
                    NotificationManager.IMPORTANCE_HIGH
                )
                channel.description = "Tool4all"
                channel.enableLights(true)
                channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                channel.lightColor = Color.BLUE

                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)

                val builder = NotificationCompat.Builder(
                    context, MyFirebaseMessagingService.CHANNEL_ID
                ).setSmallIcon(R.drawable.select_country_icon)
                    .setContentTitle(context.getString(R.string.file_downloaded))
                    .setContentText(contentText)
                    .setContentIntent(pendingIntent).setPriority(NotificationCompat.PRIORITY_HIGH)
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

    fun listingPasses(billsId: String, data: BillsDetailsAdapter.DownloadBillsDetails) {
        if (isNetworkAvailable()) {
            _checkNetDownload.value = true

            viewModelScope.launch {
                try {
                    val userToke = getUserToken()
                    userToke?.let { token ->
                        val response = Repository.getListingPasses(token, billsId)
                        if (response.isSuccessful) {
                            response.body()?.let {
                                data.onOK(it)
                            }
                        } else {
                            data.onFailed()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MyInvoicesViewModel ListingPasses", "Exception: ${e.message}")
                }
            }
        } else {
            data.onFailed()
            _checkNetDownload.value = false
        }

    }

}