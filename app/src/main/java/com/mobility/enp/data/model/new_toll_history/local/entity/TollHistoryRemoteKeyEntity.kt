package com.mobility.enp.data.model.new_toll_history.local.entity

import androidx.room.Entity

@Entity(
    tableName = "th_remote_keys",
    primaryKeys = ["queryKey"]
)
data class TollHistoryRemoteKeyEntity(
    val queryKey: String,
    val nextPage: String
)
