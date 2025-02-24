package com.mobility.enp.data.model.cardsweb


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class Data(
    @SerializedName("cardsME")
    val cardsME: List<CardsME>?,
    @SerializedName("cardsMK")
    val cardsMK: List<CardsMK>?,
    @SerializedName("cardsRS")
    val cardsRS: List<CardsRS>?,
    @SerializedName("country")
    val country: CountryXXX?,
    @SerializedName("hasSerbianCard")
    val hasSerbianCard: Boolean?,
    @SerializedName("hrTabActive")
    val hrTabActive: Boolean?,
    @SerializedName("isFranchiser")
    val isFranchiser: Boolean?,
    @SerializedName("meTabActive")
    val meTabActive: Boolean?,
    @SerializedName("mkTabActive")
    val mkTabActive: Boolean?,
    @SerializedName("rsTabActive")
    val rsTabActive: Boolean?,
    @SerializedName("showSubscriptionNotification")
    val showSubscriptionNotification: ShowSubscriptionNotification?,
    @SerializedName("showTabHR")
    val showTabHR: Boolean?,
    @SerializedName("showTabME")
    val showTabME: Boolean?,
    @SerializedName("showTabMK")
    val showTabMK: Boolean?
)