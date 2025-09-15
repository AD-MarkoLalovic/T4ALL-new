package com.mobility.enp.data.model.api_my_profile.my_tags.response

data class MyTagsList(
    val category: Category?,
    val country: Country?,
    val countryStr: String?,
    val customer: Customer?,
    val franchiser: Franchiser?,
    val id: String?,
    val registrationPlate: String?,
    val roaming: Any?,
    val serialNumber: String,
    val showButtonFoundTag: Boolean?,
    val showButtonLostTag: Boolean?,
    val state: Int?,
    val statuses: List<Statuse>?,
    val showButtonActivateTag: Boolean?,
    val showButtonDeactivateTag: Boolean?
)