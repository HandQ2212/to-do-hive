package com.proptit.todohive.ui.home.bin

import android.content.Context
import androidx.lifecycle.*
import com.proptit.todohive.data.local.AppDatabase
import com.proptit.todohive.data.local.entity.TaskEntity
import com.proptit.todohive.data.local.model.TaskWithCategory
import com.proptit.todohive.repository.TaskRepository
import kotlinx.coroutines.launch

class TaskBinViewModel(private val repo: TaskRepository) : ViewModel() {

    private val source: LiveData<List<TaskWithCategory>> = repo.observeDeleted()
    val deletedTasks: LiveData<List<TaskWithCategory>> = source

    fun onSwipeRestore(task: TaskEntity) = viewModelScope.launch {
        repo.restoreFromBin(task.task_id)
    }

    fun moveToBin(taskId: Long) = viewModelScope.launch {
        repo.moveToBin(taskId)
    }

    fun onSwipeDeleteForever(task: TaskEntity) = viewModelScope.launch {
        repo.deleteForever(task.task_id)
    }

    fun restoreAll() = viewModelScope.launch { repo.restoreAll() }
    fun clearAll() = viewModelScope.launch { repo.clearAll() }

    companion object {
        fun Factory(appContext: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(TaskBinViewModel::class.java)) {
                        val db = AppDatabase.get(appContext)
                        val prefs = appContext.getSharedPreferences("app", Context.MODE_PRIVATE)
                        val currentUserId = prefs.getLong("current_user_id", 0L)
                        require(currentUserId > 0L) { "No logged-in user." }
                        val repo = TaskRepository(db, currentUserId)
                        return TaskBinViewModel(repo) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
    }
}