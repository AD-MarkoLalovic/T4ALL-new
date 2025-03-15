package com.mobility.enp.data.repository

import android.content.Context
import com.mobility.enp.data.model.api_room_models.FcmToken
import com.mobility.enp.data.model.api_room_models.UserLoginResponseRoomTable
import com.mobility.enp.data.room.LastUser
import com.mobility.enp.data.room.database.DRoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginRepository(
    database: DRoom,
    context: Context,
) : BaseRepository(database, context) {


    fun getRoomDatabase(): DRoom {
        return database
    }

    suspend fun getLastUser(): LastUser? {
        return withContext(Dispatchers.IO) {
            database.lastUserDao().getLastUser()
        }
    }

    suspend fun storeLastUserEmail(email: String) {
        database.lastUserDao().deleteLastUser()
        database.lastUserDao().upsertLastUser(LastUser(email))
    }

    suspend fun getStoredUser(): UserLoginResponseRoomTable? {
        return withContext(Dispatchers.IO) {
            database.loginDao().fetchAllowedUsers()
        }
    }

    suspend fun writeFcmToken(token: String) {
        database.fcmToken().deleteTable()
        database.fcmToken().insertData(FcmToken(token))
    }

    suspend fun getLanguageKey(): String {
        return withContext(Dispatchers.IO) {
            getLangKey()
        }
    }
}