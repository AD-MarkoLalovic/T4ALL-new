package com.mobility.enp.data.model.api_my_invoices

import androidx.annotation.Keep

@Keep
data class BillDownload(
    val data: DataDownload?,
    val message: String? = null
)

@Keep
data class DataDownload(
    val status: String?,
    val pdfContent: String?
)
