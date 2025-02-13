package com.mobility.enp.data.repository

import android.content.Context
import com.mobility.enp.data.room.database.DRoom


class CardRepository(database: DRoom, context: Context) : BaseRepository(database, context) {


    suspend fun getLangForCard(): String? {
        return getRoomLanguage()
    }

}