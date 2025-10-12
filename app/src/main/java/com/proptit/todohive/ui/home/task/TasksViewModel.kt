package com.proptit.todohive.ui.home.task

import android.content.Context
import androidx.lifecycle.*
import com.proptit.todohive.data.local.AppDatabase
import com.proptit.todohive.data.local.entity.TaskEntity
import com.proptit.todohive.data.local.model.TaskWithCategory
import com.proptit.todohive.repository.TaskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

enum class Filter { TODAY, TOMORROW, YESTERDAY, COMPLETED }

class TasksViewModel(private val repo: TaskRepository) : ViewModel() {

    val selectedFilter = MutableLiveData(Filter.TODAY)
    fun setFilter(f: Filter) { selectedFilter.value = f }

    val filterTitle: LiveData<String> = selectedFilter.map {
        when (it) {
            Filter.TODAY -> "Today"
            Filter.TOMORROW -> "Tomorrow"
            Filter.YESTERDAY -> "Yesterday"
            Filter.COMPLETED -> "Completed"
        }
    }

    val query = MutableLiveData("")
    private val debouncedQuery = MediatorLiveData<String>().apply {
        var job: Job? = null
        addSource(query) { text ->
            job?.cancel()
            job = viewModelScope.launch {
                delay(2000)
                value = text
            }
        }
    }

    private val source: LiveData<List<TaskWithCategory>> =
        selectedFilter.switchMap {
            when (it) {
                Filter.TODAY -> repo.observeByDayRange(0)
                Filter.TOMORROW -> repo.observeByDayRange(+1)
                Filter.YESTERDAY -> repo.observeByDayRange(-1)
                Filter.COMPLETED -> repo.observeCompleted()
            }
        }

    val filteredTasks: LiveData<List<TaskWithCategory>> =
        MediatorLiveData<List<TaskWithCategory>>().apply {
            fun recompute() {
                val q = debouncedQuery.value.orEmpty().trim().lowercase(Locale.getDefault())
                val base = source.value.orEmpty()
                val out = if (q.isEmpty()) base else base.filter {
                    val title = it.task.title.lowercase(Locale.getDefault())
                    val cat = (it.category?.name ?: "").lowercase(Locale.getDefault())
                    val time = it.task.even_at.toString().lowercase(Locale.getDefault())
                    title.contains(q) || cat.contains(q) || time.contains(q)
                }
                value = out.sortedBy { it.task.even_at }
            }
            addSource(source) { recompute() }
            addSource(debouncedQuery) { recompute() }
        }

    fun setQuery(text: String) { query.value = text }

    fun onToggleDone(task: TaskEntity) = viewModelScope.launch {
        repo.toggleCompleted(task.task_id)
    }

    fun onSwipeDelete(task: TaskEntity) = viewModelScope.launch {
        repo.delete(task.task_id)
    }

    fun restore(task: TaskEntity) = viewModelScope.launch {
        repo.restore(task)
    }

    companion object {
        fun Factory(appContext: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
                        val db = AppDatabase.get(appContext)
                        val prefs = appContext.getSharedPreferences("app", Context.MODE_PRIVATE)
                        val currentUserId = prefs.getLong("current_user_id", 0L)
                        require(currentUserId > 0L) { "No logged-in user." }
                        val repo = TaskRepository(db, currentUserId)
                        return TasksViewModel(repo) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
    }
}