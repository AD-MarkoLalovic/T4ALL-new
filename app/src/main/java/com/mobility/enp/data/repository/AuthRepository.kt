package com.mobility.enp.data.repository

import android.content.Context
import com.mobility.enp.data.model.api_room_models.UserLanguage
import com.mobility.enp.data.model.api_room_models.UserLoginResponseRoomTable
import com.mobility.enp.data.room.database.DRoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Odgovornost: Upravljanje autentifikacijom i funkcionalnostima za korisnički nalog.
 * Logovanje korisnika, Promena lozinke,Resetovanje lozinke,Deaktivacija korisničkog naloga.
 * Promena jezika.
 */

class AuthRepository(database: DRoom, context: Context) : BaseRepository(database, context) {

    /**
     * Language picker
     */
    suspend fun getAllowedUserLanguage(): UserLanguage? {
        return database.languageDao().fetchAllowedUsers()
    }

    suspend fun clearLanguages() {
        database.languageDao().deleteAll()
    }

    suspend fun saveLanguage(language: UserLanguage) {
        database.languageDao().insertLanguage(language)
    }

    suspend fun getToken(): UserLoginResponseRoomTable? {
        return withContext(Dispatchers.IO) {
            database.loginDao().fetchAllowedUsers()
        }
    }

}