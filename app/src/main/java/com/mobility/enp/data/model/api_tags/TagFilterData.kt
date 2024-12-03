package com.mobility.enp.data.model.api_tags

import androidx.annotation.Keep

@Keep
data class TagFilterData(
    val tagValue: Int,
    val tagText: String
)
