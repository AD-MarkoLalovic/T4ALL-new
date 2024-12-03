package com.mobility.enp.data.model.api_tags

import androidx.annotation.Keep

// used for complaint response as well
@Keep
data class LostTagResponse(
    val data: Dat,
    val message: String
)

@Keep
data class Dat(
    val status: String,
    val message: String
)

@Keep
data class Errors(
    val serialNumber: List<String>
)
