package com.mobility.enp.data.model.franchise

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable

data class FranchiseModel(
    val franchiseId: String,
    val franchiseName: String,
    val franchisePrimaryColor: Int,
    val franchiseHomeBackgroundLocation: Drawable?,
    val franchiseProfileResource: Int?,
    val franchiseLogoToolbar:Drawable?,
    val enableBackgroundColorOnToolBar:Boolean,
    val navHomeDrawable : ColorStateList?,
    val promotionsDot:Int?,
    val halfColor : Int,
    val backButtonResource :Int,
    val downArrowResource : Int,
    val upArrowResource : Int,
    val rightArrowResource : Int,
    val loopIcon: Int,
    val cameraResource : Int,
    val languageIcon : Int,
    val calendarResource : Int,
    val franchisePlusButton : Int,
    val franchiseCloseButton : Int,
    val franchiseProfilePictureResource : Int,
    val homePageWelcomeTextColor : ColorStateList?,
    val franchiseFlavorTextColor : ColorStateList?,
    val franchiseCalendarStyle : Int,
    val franchiseFlavorText: String = "Powered by Toll4All",
)
