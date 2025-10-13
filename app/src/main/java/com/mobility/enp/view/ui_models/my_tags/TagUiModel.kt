package com.mobility.enp.view.ui_models.my_tags

data class TagUiModel(
    val serialNumber: String,
    val registrationPlate: String?,
    val countryCode: String?,
    val countryName: String?,
    val statuses: List<TagStatusUiModel>,
    val showButtonLostTag: Boolean?,
    val showButtonFoundTag: Boolean?,
    val category: Int?,
    val franchiser: String?,
    var showButtonActivateTag: Boolean?,  // requires country code filter or api will always send false ...
    var showButtonDeactivateTag: Boolean?
)


