package com.mobility.enp.data.model.new_toll_history.response

data class Data(
    val allowedCountries: List<AllowedCountry?>?,
    val customer: Customer?,
    val records: Records?,
    val sumTags: List<SumTag?>?,
    val tags: List<Any?>?
)