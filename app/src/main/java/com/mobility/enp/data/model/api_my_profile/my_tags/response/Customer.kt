package com.mobility.enp.data.model.api_my_profile.my_tags.response

import com.google.gson.annotations.SerializedName

data class Customer(
    val address: String?,
    val city: String?,

    @SerializedName("company_name")
    val companyName: String?,

    @SerializedName("company_name_invoice")
    val companyNameInvoice: Any?,

    @SerializedName("contract_number")
    val contractNumber: Long?,

    val country: String?,

    @SerializedName("created_at")
    val createdAt: String?,

    @SerializedName("created_by_api_user")
    val createdByApiUser: Any?,

    @SerializedName("cro_id")
    val croId: String?,

    @SerializedName("customer_type")
    val customerType: Int?,

    val discount: Int?,

    @SerializedName("discount_secondary")
    val discountSecondary: Int?,

    @SerializedName("display_name")
    val displayName: String?,

    val email: String?,

    @SerializedName("first_name")
    val firstName: String?,

    @SerializedName("franchiser_id")
    val franchiserId: Any?,

    val id: Int?,

    val jbkjs: Any?,
    val jmbg: Any?,

    @SerializedName("last_name")
    val lastName: String?,

    val mb: String?,
    val note: Any?,

    @SerializedName("payment_channel")
    val paymentChannel: Int?,

    val phone: String?,
    val pib: String?,

    @SerializedName("postal_code")
    val postalCode: String?,

    @SerializedName("tag_country")
    val tagCountry: String?,

    @SerializedName("updated_at")
    val updatedAt: String?,

    @SerializedName("user_id")
    val userId: Int?
)
