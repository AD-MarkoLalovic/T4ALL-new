package com.mobility.enp.data.model.api_my_profile.basic_information.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mobility.enp.view.ui_models.BasicInfoUIModel

@Entity(tableName = "basic_user_info")
data class BasicInfoEntity(

    @PrimaryKey
    val id: Int,
    val address: String,
    val city: String,
    val companyName: String?,
    val countryCode: String,
    val countryName: String,
    val customerType: Int,
    val displayName: String?,
    val email: String,
    val firstName: String?,
    val isFranchiser: Boolean,
    val language: String,
    val lastName: String?,
    val mb: String?,
    val phone: String,
    val postalCode: String?
) {
    fun toUIModel(): BasicInfoUIModel {
        return BasicInfoUIModel(
            address = address,
            city = city,
            companyName = companyName,
            countryName = countryName,
            customerType = customerType,
            displayName = displayName,
            email = email,
            firstName = firstName,
            lastName = lastName,
            mb = mb,
            phone = phone,
            postalCode = postalCode
        )
    }
}
