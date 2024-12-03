package com.mobility.enp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mobility.enp.data.room.database.DRoom

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {

    val database = DRoom.buildDatabase(application)

    val notificationList = database.notificationDao().fetchNotifications()
}