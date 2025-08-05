package com.mobility.enp.data.repository

import android.content.Context
import com.mobility.enp.data.room.database.DRoom

class FranchiserRepository(
    database: DRoom,
    context: Context,
) : BaseRepository(database, context) {
    private val homeDao = database.homeScreenDao()

    suspend fun getPortalKey(): String? {
        return database.loginDao().fetchAllowedUsers().portalKey
    }
}