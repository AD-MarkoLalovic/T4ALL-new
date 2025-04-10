package com.mobility.enp.data.model.login

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
data class UserResponse(
    @SerializedName("data")
    @Expose
    val data: Data?,
    @SerializedName("message")
    @Expose
    val message: String?
) : Serializable

@Keep
data class Data(
    @SerializedName("access_token")
    @Expose
    val accessToken: String?,
    @SerializedName("token_type")
    @Expose
    val tokenType: String?,
    @Expose
    val portal_key: String?
) : Serializable