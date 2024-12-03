package com.mobility.enp.util

import android.content.Context
import com.mobility.enp.data.model.IntroPageStatus
import com.mobility.enp.data.room.database.DRoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IntroScreensRepository(context: Context) {

    val database: DRoom = DRoom.getRoomInstance(context)

    suspend fun saveIntroPageShown(key: String, value: Boolean) {
        withContext(Dispatchers.IO) {
            database.introStateDao().deleteAll()
            val introPageStatus = IntroPageStatus(key, value)
            database.introStateDao().saveIntroPageShown(introPageStatus)
        }
    }

    suspend fun getIntroPageShow(key: String): Boolean = withContext(Dispatchers.IO) {
        database.introStateDao().getByKey(key)
    }

}
