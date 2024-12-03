package com.mobility.enp.data.model.banks.response

import com.google.gson.annotations.SerializedName
import com.mobility.enp.data.model.banks.entity.BanksEntity

data class DataX(
    val id: Int,
    val name: String,
    @SerializedName("unique_numbers")
    val uniqueNumber: List<Int>
) {
    fun toBanksEntity() : BanksEntity {
        return BanksEntity (
            id = id,
            name = name,
            uniqueNumber = uniqueNumber
        )
    }
}