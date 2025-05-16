package com.mobility.enp.data.model.cards.tags_for_croatia

data class Tag(
    val category: Category?,
    val country: Country?,
    val id: String?,
    val registrationPlate: String,
    val roming: Boolean?,
    val serialNumber: String,
    val showButtonFoundTag: Boolean?,
    val showButtonLostTag: Boolean?,
    val statuses: List<Statuses>?
)