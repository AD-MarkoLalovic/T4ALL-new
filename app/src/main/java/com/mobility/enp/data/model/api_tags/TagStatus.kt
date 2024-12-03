package com.mobility.enp.data.model.api_tags

import androidx.annotation.Keep

@Keep
data class TagStatus(
    val id: String?,
    val serialNumber: String?,
    val registrationPlate: String?,
    val country: Country?,
    val category: Category?,
    var status: Status?,
    val showButtonLostTag: Boolean,
    val showButtonFoundTag: Boolean
)
