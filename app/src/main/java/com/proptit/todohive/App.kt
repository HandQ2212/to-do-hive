package com.proptit.todohive

import android.app.Application
import com.cloudinary.android.MediaManager
import com.proptit.todohive.data.local.AppDatabase
import com.proptit.todohive.data.local.seedIfEmpty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.proptit.todohive.notification.NotificationService
import android.app.NotificationChannel
import android.app.NotificationManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        //Init MediaManager with Cloudinary config
        initCloudinaryConfigToMediaManager()

        // Notification
        createNotificationChannel()

        // App Database
        val db = AppDatabase.get(this)
        CoroutineScope(Dispatchers.IO).launch { seedIfEmpty(db) }
    }
    private fun initCloudinaryConfigToMediaManager() {
        val config = hashMapOf(
            "cloud_name" to BuildConfig.CLOUD_NAME,
            "upload_preset" to BuildConfig.UPLOAD_PRESET,
            "secure" to "true"
        )
        MediaManager.init(this, config)
    }
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NotificationService.CHANNEL_ID,
            "Todo",
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Used for the notice todo" }
        val notificationManager = getSystemService(NotificationManager::class.java) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}