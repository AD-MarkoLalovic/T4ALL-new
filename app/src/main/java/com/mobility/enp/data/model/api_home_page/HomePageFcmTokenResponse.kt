package com.mobility.enp.data.model.api_home_page

import androidx.annotation.Keep

@Keep
data class HomePageFcmTokenResponse(
    val data: List<Any>,
    val message: String
)
