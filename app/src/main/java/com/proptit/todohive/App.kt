package com.proptit.todohive

import android.app.Application
import com.cloudinary.android.MediaManager
import com.proptit.todohive.data.local.AppDatabase
import com.proptit.todohive.data.local.seedIfEmpty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        //Init MediaManager with Cloudinary config
        val config = hashMapOf(
            "cloud_name" to BuildConfig.CLOUD_NAME,
            "upload_preset" to BuildConfig.UPLOAD_PRESET,
            "secure" to "true"
        )
        MediaManager.init(this, config)

        // App Database
        val db = AppDatabase.get(this)
        CoroutineScope(Dispatchers.IO).launch { seedIfEmpty(db) }
    }
}