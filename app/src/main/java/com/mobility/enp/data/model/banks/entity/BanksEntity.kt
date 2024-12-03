package com.mobility.enp.data.model.banks.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mobility.enp.view.ui_models.BankUIModel

@Entity(tableName = "banks")
data class BanksEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val uniqueNumber: List<Int>
) {
    fun toBanksUIModel(): BankUIModel {
        return BankUIModel(
            id = id,
            bankName = name,
            uniqueNumber = uniqueNumber
        )
    }
}
