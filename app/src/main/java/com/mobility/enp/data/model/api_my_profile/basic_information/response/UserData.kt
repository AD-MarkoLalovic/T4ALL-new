package com.mobility.enp.data.model.api_my_profile.basic_information.response

import com.mobility.enp.data.model.api_my_profile.basic_information.entity.BasicInfoEntity

data class UserData(
    val address: String,
    val city: String,
    val companyName: String?,
    val country: Country,
    val customerType: CustomerType,
    val displayName: String,
    val email: String,
    val firstName: String?,
    val id: Int,
    val isFranchiser: Boolean,
    val language: String,
    val lastName: String?,
    val mb: String?,
    val phone: String,
    val postalCode: String?,
    val pib: String?
) {
    fun toEntity(): BasicInfoEntity {
        return BasicInfoEntity (
            id = id,
            address = address,
            city = city,
            companyName = companyName,
            countryCode = country.code,
            countryName = country.name,
            customerType = customerType.type,
            displayName = displayName,
            email = email,
            firstName = firstName,
            isFranchiser = isFranchiser,
            language = language,
            lastName = lastName,
            mb = mb,
            phone = phone,
            postalCode = postalCode,
            pib = pib

        )
    }
}