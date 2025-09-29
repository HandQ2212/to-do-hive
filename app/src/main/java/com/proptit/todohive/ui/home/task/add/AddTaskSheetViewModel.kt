package com.proptit.todohive.ui.home.task.add

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.proptit.todohive.common.TimeFormatUtils
import com.proptit.todohive.data.local.AppDatabase
import com.proptit.todohive.repository.TaskRepository
import kotlinx.coroutines.launch
import java.time.Instant

class AddTaskSheetViewModel(app: Application) : AndroidViewModel(app) {

    val title = MutableLiveData("")
    val description = MutableLiveData("")

    val pickedInstant = MutableLiveData<Instant?>(null)
    val pickedCategoryId = MutableLiveData<Long?>(null)
    val pickedPriority = MutableLiveData(1)

    val timeText: LiveData<String> =
        pickedInstant.map { it?.let(TimeFormatUtils::formatInstantShort) ?: "No time" }

    val categoryText: LiveData<String> =
        pickedCategoryId.map { id ->
            when (id) {
                null -> "None"
                1L -> "University"
                2L -> "Home"
                3L -> "Work"
                else -> "Category #$id"
            }
        }

    val priorityText: LiveData<String> =
        pickedPriority.map { p -> "P$p" }

    val isSaveEnabled: LiveData<Boolean> =
        title.map { !it.isNullOrBlank() }

    private val repo by lazy { TaskRepository(AppDatabase.get(getApplication())) }

    fun setPickedInstant(instant: Instant) {
        pickedInstant.value = instant
    }

    fun setPickedCategoryId(id: Long?) {
        pickedCategoryId.value = id
    }

    fun setPickedPriority(p: Int) {
        pickedPriority.value = p.coerceIn(1, 10)
    }

    fun save(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        val t = title.value?.trim().orEmpty()
        val at = pickedInstant.value ?: Instant.now()
        val catId = pickedCategoryId.value
        val prior = pickedPriority.value ?: 1
        val desc = description.value?.trim().orEmpty()

        viewModelScope.launch {
            runCatching {
                repo.create(
                    title = t,
                    at = at,
                    priority = prior,
                    categoryId = catId,
                    description = desc
                )
            }.onSuccess {
                onSuccess()
                reset()
            }.onFailure(onError)
        }
    }

    fun reset() {
        title.value = ""
        description.value = ""
        pickedInstant.value = null
        pickedCategoryId.value = null
        pickedPriority.value = 1
    }
    fun clearPickedInstant() {
        pickedInstant.value = null
    }
}