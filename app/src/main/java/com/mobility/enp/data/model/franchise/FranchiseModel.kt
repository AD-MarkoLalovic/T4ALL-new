package com.mobility.enp.data.model.franchise

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable

data class FranchiseModel(
    val franchiseId: String,
    val franchiseName: String,
    val franchisePrimaryColor: Int,
    val franchiseHomeBackgroundLocation: Drawable?,
    val franchiseProfileResource: Drawable?,
    val franchiseLogoToolbar:Drawable?,
    val enableBackgroundColorOnToolBar:Boolean,
    val navHomeDrawable : ColorStateList?
)
