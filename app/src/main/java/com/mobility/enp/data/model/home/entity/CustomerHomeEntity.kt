package com.mobility.enp.data.model.home.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mobility.enp.view.ui_models.home.HomeCustomerUI

@Entity(tableName = "customer_info_home")
data class CustomerHomeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val firstName: String,
    val lastName: String,
    val displayName: String,
    val customerType: Int?
) {
    fun toHomeCustomerUI(): HomeCustomerUI {
        return HomeCustomerUI(
            displayName = displayName
        )
    }
}

