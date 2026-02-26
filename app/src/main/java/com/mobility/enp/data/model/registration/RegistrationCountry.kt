package com.mobility.enp.data.model.registration

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class RegistrationCountry(
    val code: String,

    @param:StringRes
    val name: Int,

    @param:DrawableRes
    val flagResId: Int
)
