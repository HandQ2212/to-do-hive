package com.proptit.todohive.ui.home.task

import android.content.Context
import androidx.lifecycle.*
import com.proptit.todohive.data.local.AppDatabase
import com.proptit.todohive.data.local.entity.CategoryEntity
import com.proptit.todohive.data.local.entity.TaskEntity
import com.proptit.todohive.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TaskDetailViewModel(private val repo: TaskRepository) : ViewModel() {

    private val _task = MutableLiveData<TaskEntity?>()
    val task: LiveData<TaskEntity?> = _task

    val taskTitle     = MutableLiveData<String>()
    val taskDesc      = MutableLiveData<String>()
    val taskTimeText  = MutableLiveData<String>()
    val categoryText  = MutableLiveData<String>()
    val priorityText  = MutableLiveData<String>()
    val isDone        = MutableLiveData<Boolean>()
    val category      = MutableLiveData<CategoryEntity?>()

    private val formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
    private val zone = ZoneId.systemDefault()

    private var currentId: Long? = null

    fun load(taskId: Long) = viewModelScope.launch {
        currentId = taskId
        val t = repo.getById(taskId) ?: return@launch
        _task.value = t
        taskTitle.value    = t.title
        taskDesc.value     = t.description ?: ""
        taskTimeText.value = formatter.format(t.even_at.atZone(zone))
        priorityText.value = "P${t.priority}"
        isDone.value       = t.is_completed
        val cat = withContext(Dispatchers.IO) { repo.getCategoryById(t.category_id) }
        category.value     = cat
        categoryText.value = cat?.name ?: "Default"
    }

    fun toggleDone() = viewModelScope.launch {
        val id = currentId ?: return@launch
        repo.toggleCompleted(id)
        isDone.value = !(isDone.value ?: false)
    }

    fun editTitle(newTitle: String? = null) = viewModelScope.launch {
        val t = _task.value ?: return@launch
        val title = newTitle ?: t.title
        repo.update(
            id = t.task_id,
            title = title,
            at = t.even_at,
            priority = t.priority,
            categoryId = t.category_id,
            description = t.description ?: ""
        )
        taskTitle.value = title
    }

    fun pickTime(newInstant: Instant? = null) = viewModelScope.launch {
        val t = _task.value ?: return@launch
        val instant = newInstant ?: t.even_at
        _task.value = t.copy(even_at = instant)
        taskTimeText.value = formatter.format(instant.atZone(zone))
        repo.update(
            id = t.task_id,
            title = t.title,
            at = instant,
            priority = t.priority,
            categoryId = t.category_id,
            description = t.description ?: ""
        )
    }

    fun pickCategory(categoryId: Long? = null, categoryName: String? = null) = viewModelScope.launch {
        val t = _task.value ?: return@launch
        _task.value = t.copy(category_id = categoryId)
        val cat = withContext(Dispatchers.IO) { repo.getCategoryById(categoryId) }
        category.value     = cat
        categoryText.value = categoryName ?: cat?.name ?: "Default"
        repo.update(
            id = t.task_id,
            title = t.title,
            at = t.even_at,
            priority = t.priority,
            categoryId = categoryId,
            description = t.description ?: ""
        )
    }

    fun pickPriority(priority: Int = 1) = viewModelScope.launch {
        val t = _task.value ?: return@launch
        _task.value = t.copy(priority = priority)
        priorityText.value = "P$priority"
        repo.update(
            id = t.task_id,
            title = t.title,
            at = t.even_at,
            priority = priority,
            categoryId = t.category_id,
            description = t.description ?: ""
        )
    }

    fun deleteTask() = viewModelScope.launch {
        val id = currentId ?: return@launch
        repo.delete(id)
    }

    fun editTask(
        newTitle: String = taskTitle.value ?: "",
        newDesc: String = taskDesc.value ?: ""
    ) = viewModelScope.launch {
        val t = _task.value ?: return@launch
        repo.update(
            id = t.task_id,
            title = newTitle,
            at = t.even_at,
            priority = t.priority,
            categoryId = t.category_id,
            description = newDesc
        )
        taskTitle.value = newTitle
        taskDesc.value  = newDesc
    }

    companion object {
        fun Factory(appContext: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(TaskDetailViewModel::class.java)) {
                        val db = AppDatabase.get(appContext)
                        val repo = TaskRepository(db)
                        return TaskDetailViewModel(repo) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
    }
}