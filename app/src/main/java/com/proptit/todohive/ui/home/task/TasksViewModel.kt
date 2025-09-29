package com.proptit.todohive.ui.home.task

import androidx.lifecycle.*
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
            val t = q.trim().lowercase()
            val filtered = if (t.isBlank()) list else list.filter {
                val title = it.task.title.lowercase()
                val cat   = (it.category?.name ?: "").lowercase()
                val time  = it.task.even_at.toString().lowercase()
                title.contains(t) || cat.contains(t) || time.contains(t)
            }
            filtered.sortedBy { item -> item.task.even_at }
        }.asLiveData()
}
