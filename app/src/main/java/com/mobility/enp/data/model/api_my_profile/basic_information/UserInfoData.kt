package com.mobility.enp.data.model.api_my_profile.basic_information

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_information")
@Keep
data class UserInfoData(

    @PrimaryKey
    val id: Int,
    val address: String?,
    val city: String?,
    val companyName: String?,
    val country: Country?,
    val customerType: CustomerType?,
    val displayName: String?,
    val email: String?,
    val firstName: String?,
    val language: String?,
    val lastName: String?,
    val mb: String?,
    val phone: String?,
    val postalCode: String?,
    val isFranchiser: Boolean
)