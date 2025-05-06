package com.mobility.enp.data.repository

import android.content.Context
import com.mobility.enp.data.model.ProfileImage
import com.mobility.enp.data.model.api_room_models.FcmToken
import com.mobility.enp.data.room.database.DRoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//changed this to be for profile fragment because it was not used initial idea

class ProfileRepository(database: DRoom, context: Context) : BaseRepository(database, context) {

    suspend fun userToken(): String? {
        return getUserToken()
    }

    fun getLanguageKey(): String {
        return getLangKey()
    }

    suspend fun deleteDatabase() {
        withContext(Dispatchers.IO) {
            database.clearAllData()
        }
    }

    suspend fun getFcmData(): FcmToken? {
        return withContext(Dispatchers.IO) {
            database.fcmToken().getTableData()
        }
    }

    suspend fun deleteProfilePicture() {
        database.profileImageDao().deleteAll()
    }

    suspend fun getStoredImage(): List<ProfileImage>? {
        return withContext(Dispatchers.IO) {
            database.profileImageDao().selectAll()
        }
    }

    fun isNetworkAvail(): Boolean {
        return isNetworkAvailable()
    }

}