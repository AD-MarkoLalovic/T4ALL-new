package com.mobility.enp.data.model.new_toll_history.local.entity

data class TollHistoryEntities(
    val countries: List<AllowedCountryEntity>,
    val sumTags: List<SumTagEntity>,
    val items: List<TollHistoryItemEntity>
)
