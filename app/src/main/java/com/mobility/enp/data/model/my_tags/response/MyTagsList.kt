package com.mobility.enp.data.model.my_tags.response

data class MyTagsList(
    val category: Category?,
    val country: Country?,
    val id: String?,
    val registrationPlate: String?,
    val roming: Boolean?,
    val serialNumber: String?,
    val showButtonFoundTag: Boolean?,
    val showButtonLostTag: Boolean?,
    val statuses: List<Statuse>
)