package com.mobility.enp.data.model.registration

import android.graphics.drawable.Drawable
import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class CountryModel(
    val countryCode: String?,
    val countryName: String?,
    val countryFlag: Drawable?,
    var isChecked: Boolean,
    var isIconVisible: Boolean,
    var isTosChecked: Boolean,
    var isViewVisible: Boolean
) : Serializable {
    constructor(countryCode: String?, countryName: String?, countryFlag: Drawable?) : this(
        countryCode,
        countryName,
        countryFlag,
        false,
        true,
        false,
        false
    )

    constructor(countryName: String?, countryFlag: Drawable?) : this(
        null,
        countryName,
        countryFlag,
        false,
        true,
        false,
        false
    )
}