package com.proptit.todohive.ui.home.task

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.proptit.todohive.data.local.AppDatabase
import com.proptit.todohive.repository.TaskRepository

class TasksViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
            val db = AppDatabase.get(appContext)
            val repo = TaskRepository(db)
            return TasksViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
