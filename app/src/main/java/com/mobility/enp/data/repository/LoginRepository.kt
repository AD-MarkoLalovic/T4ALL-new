package com.mobility.enp.data.repository

import android.content.Context
import com.mobility.enp.data.room.database.DRoom

class LoginRepository ( database: DRoom,
                        context: Context,) : BaseRepository(database,context) {


    fun getRoomDatabase(): DRoom{
        return database
    }
}