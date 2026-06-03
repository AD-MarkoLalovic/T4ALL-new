package com.mobility.enp.data.model.api_my_invoices

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobility.enp.data.model.api_my_invoices.refactor.AllowedCountry

@Keep
data class RsBillsResponse(
    @SerializedName("data") @Expose val data: RsBillsData?,
    @SerializedName("message") @Expose val message: String?
)

@Keep
data class RsBillsData(
    @SerializedName("bills") @Expose val bills: RsBills?,
    @SerializedName("showTollCollectionNotification") @Expose val showTollCollectionNotification: Boolean = false,
    @SerializedName("message") @Expose val message: String?,
    @SerializedName("totalSumData") @Expose val totalSumData: List<RsTotalSumData>?,
    @SerializedName("allowedCountries") @Expose val allowedCountries: List<AllowedCountry>?
)

@Keep
data class RsBills(
    @SerializedName("pagination") @Expose val pagination: RsBillsPagination?,
    @SerializedName("items") @Expose val items: List<RsBillItem>?
)

@Keep
data class RsBillsPagination(
    @SerializedName("current_page") @Expose val currentPage: Int,
    @SerializedName("prev_page") @Expose val prevPage: Int,
    @SerializedName("next_page") @Expose val nextPage: Int,
    @SerializedName("first_page") @Expose val firstPage: Int,
    @SerializedName("last_page") @Expose val lastPage: Int,
    @SerializedName("total") @Expose val total: Int,
    @SerializedName("per_page") @Expose val perPage: Int
)

@Keep
data class RsBillItem(
    @SerializedName("paidLabel") @Expose val paidLabel: String?,
    @SerializedName("isUnpaid") @Expose val isUnpaid: Boolean,
    @SerializedName("id") @Expose val id: String?,
    @SerializedName("currency") @Expose val currency: String?,
    @SerializedName("paid") @Expose val paid: Int,
    @SerializedName("billFinal") @Expose val billFinal: String?,
    @SerializedName("total") @Expose val total: String?,
    @SerializedName("dateOfIssue") @Expose val dateOfIssue: String?,
    @SerializedName("datePaid") @Expose val datePaid: String?,
    @SerializedName("discount") @Expose val discount: Int
)

@Keep
data class RsTotalSumData(
    @SerializedName("currencyLabel") @Expose val currencyLabel: String?,
    @SerializedName("currency") @Expose val currency: String?,
    @SerializedName("amount") @Expose val amount: String?
)

fun RsBillItem.toBill(): Bill = Bill(
    id = id,
    billFinal = billFinal,
    total = total,
    currency = BillsDetailsCurrency(value = currency, text = currency),
    country = null,
    dateOfIssue = dateOfIssue,
    datePaid = datePaid,
    status = Status(value = if (paid == 1) 1 else 0, text = paidLabel ?: ""),
    discount = null
)

fun RsBillsResponse.toBillData(): BillData {
    val pagination = data?.bills?.pagination
    val items = data?.bills?.items?.map { it.toBill() } ?: emptyList()
    return BillData(
        bills = items,
        total = pagination?.total ?: items.size,
        currentPage = pagination?.currentPage ?: 1,
        perPage = pagination?.perPage ?: items.size,
        lastPage = pagination?.lastPage ?: 1
    )
}
