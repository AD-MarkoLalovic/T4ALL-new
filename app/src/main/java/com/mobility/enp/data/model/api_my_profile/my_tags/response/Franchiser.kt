package com.mobility.enp.data.model.api_my_profile.my_tags.response

import com.google.gson.annotations.SerializedName

data class Franchiser(
    val address: String?,

    @SerializedName("brand_name")
    val brandName: String?,

    val city: String?,

    @SerializedName("commission_interest")
    val commissionInterest: Int?,

    @SerializedName("company_name")
    val companyName: String?,

    @SerializedName("contract_number")
    val contractNumber: String?,

    val country: String?,

    @SerializedName("created_at")
    val createdAt: String?,

    @SerializedName("display_name")
    val displayName: String?,

    val email: String?,
    val id: String?,
    val jbkjs: Any?,

    @SerializedName("logo_path")
    val logoPath: Any?,

    val mb: String?,

    @SerializedName("parent_id")
    val parentId: Any?,

    val phone: String?,
    val pib: String?,

    @SerializedName("postal_code")
    val postalCode: Any?,

    @SerializedName("status_active")
    val statusActive: Int?,

    @SerializedName("updated_at")
    val updatedAt: String?,

    @SerializedName("user_id")
    val userId: Int?
)
