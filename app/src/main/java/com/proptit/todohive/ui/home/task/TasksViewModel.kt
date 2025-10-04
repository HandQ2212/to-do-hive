package com.proptit.todohive.ui.home.task

import android.content.Context
import androidx.lifecycle.*
import com.proptit.todohive.data.local.AppDatabase
import com.proptit.todohive.data.local.entity.TaskEntity
import com.proptit.todohive.data.local.model.TaskWithCategory
import com.proptit.todohive.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Filter { TODAY, TOMORROW, YESTERDAY, COMPLETED }

class TasksViewModel(private val repo: TaskRepository) : ViewModel() {

    val query = MutableLiveData("")

    private val _selectedFilter = MutableLiveData(Filter.TODAY)
    val selectedFilter: LiveData<Filter> = _selectedFilter
    fun setFilter(f: Filter) { _selectedFilter.value = f }

    val filterTitle: LiveData<String> = selectedFilter.map { f ->
        when (f) {
            Filter.TODAY -> "Today"
            Filter.TOMORROW -> "Tomorrow"
            Filter.YESTERDAY -> "Yesterday"
            Filter.COMPLETED -> "Completed"
        }
    }

    private val sourceFlow: Flow<List<TaskWithCategory>> =
        selectedFilter.asFlow().flatMapLatest { f ->
            when (f) {
                Filter.TODAY     -> repo.observeByDayRange(0)
                Filter.TOMORROW  -> repo.observeByDayRange(+1)
                Filter.YESTERDAY -> repo.observeByDayRange(-1)
                Filter.COMPLETED -> repo.observeCompleted()
            }
        }

    val filteredTasks: LiveData<List<TaskWithCategory>> =
        query.asFlow().debounce(50).combine(sourceFlow) { q, list ->
            val other = q.trim().lowercase()
            val filtered = if (other.isBlank()) list else list.filter {
                val title = it.task.title.lowercase()
                val cat   = (it.category?.name ?: "").lowercase()
                val time  = it.task.even_at.toString().lowercase()
                title.contains(other) || cat.contains(other) || time.contains(other)
            }
            filtered.sortedBy { item -> item.task.even_at }
        }.asLiveData()

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
                        val repo = TaskRepository(db)
                        return TasksViewModel(repo) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
    }
}
