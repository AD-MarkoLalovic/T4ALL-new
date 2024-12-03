package com.mobility.enp.data.model.api_home_page.homedata


import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Keep
@Entity("HomeData")
data class HomeScreenData(
    @PrimaryKey
    val id: Int = 1,
    @SerializedName("data")
    var `data`: Data?,
    @SerializedName("message")
    var message: String?
)