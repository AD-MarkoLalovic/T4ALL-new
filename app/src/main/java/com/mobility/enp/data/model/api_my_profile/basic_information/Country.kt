package com.mobility.enp.data.model.api_my_profile.basic_information

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class Country(
    val code: String,
    val name: String
) : Serializable