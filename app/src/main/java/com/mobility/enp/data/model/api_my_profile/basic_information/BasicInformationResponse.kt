package com.mobility.enp.data.model.api_my_profile.basic_information

import androidx.annotation.Keep

@Keep
data class BasicInformationResponse(
    val `data`: UserInfoData?,
    val message: String
)