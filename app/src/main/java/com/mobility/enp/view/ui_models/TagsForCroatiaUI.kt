package com.mobility.enp.view.ui_models

data class TagsForCroatiaUI(
    val serialNumberUI: String,
    val registrationPlateUI: String,
    val status: Int?,
    var selected: Boolean = false,
    val currentPage: Int,
    val lastPage: Int,
    val perPage: Int
)
