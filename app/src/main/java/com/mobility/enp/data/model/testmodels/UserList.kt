package com.mobility.enp.data.model.testmodels

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


@Keep
data class UserList(
    @SerializedName("page")
    @Expose
    val page: Int,
    @SerializedName("per_page")
    @Expose
    val per_page: Int,
    @SerializedName("total")
    @Expose
    val total: Int,
    @SerializedName("total_pages")
    @Expose
    val total_pages: Int,
    @SerializedName("data")
    @Expose
    val data: List<UserListData>
) : Serializable


data class UserListData(
    @SerializedName("id")
    @Expose
    val id: Int,
    @SerializedName("email")
    @Expose
    val email: String,
    @SerializedName("first_name")
    @Expose
    val first_name: String,
    @SerializedName("last_name")
    @Expose
    val last_name: String,
    @SerializedName("avatar")
    @Expose
    val avatar: String
) : Serializable