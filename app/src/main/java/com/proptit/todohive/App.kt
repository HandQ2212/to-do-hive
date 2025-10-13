package com.proptit.todohive

import android.app.Application
import com.proptit.todohive.data.local.AppDatabase
import com.proptit.todohive.data.local.seedIfEmpty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.get(this)
        CoroutineScope(Dispatchers.IO).launch { seedIfEmpty(db) }
    }
}