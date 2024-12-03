package com.mobility.enp.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mobility.enp.R
import com.mobility.enp.data.model.notification.NotificationModel
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.view.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val TAG = "FirebaseFcm"
        const val CHANNEL_ID = "my_channel_id"
        const val NOTIFICATION_ID = 1
    }

    fun createNotification(context: Context, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE)

        // Create a notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Tool4all",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Tool4all"
            channel.enableLights(true)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            channel.lightColor = Color.BLUE

            val notificationManager =
                context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.select_country_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Display the notification
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "createNotification: permissions not granted")
            return
        }
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())

    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "object: $message")


        // Check if message contains a notification payload.
        message.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            createNotification(this, "${it.title}", "${it.body}")
            saveToRoom(it)
        }
    }

    private fun saveToRoom(notification: RemoteMessage.Notification) {
        CoroutineScope(Dispatchers.IO).launch {
            val database = DRoom.buildDatabase(applicationContext)
            val message =
                NotificationModel(notification.title, notification.body, System.currentTimeMillis())
            database.notificationDao().insert(message)
        }
    }

}