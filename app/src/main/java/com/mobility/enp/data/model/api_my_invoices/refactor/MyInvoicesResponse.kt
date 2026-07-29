package com.mobility.enp.data.model.api_my_invoices.refactor


import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "monthly_bills")
data class MyInvoicesResponse(
    @PrimaryKey
    val id: Int,
    @SerializedName("data")
    @Expose
    val `data`: Data?,
    @SerializedName("message")
    @Expose
    val message: String?,
    var country: String
)