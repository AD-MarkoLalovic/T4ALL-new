package com.mobility.enp.data.model.api_tool_history.v2base_model

import androidx.room.Entity

@Entity(
    tableName = "allowed_countries",
    primaryKeys = ["country"]
)
data class V2AllowedCountries(
    val country: String
)