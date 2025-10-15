package com.proptit.todohive.ui.home.calendar

import androidx.lifecycle.*
import com.proptit.todohive.data.local.entity.TaskEntity
import com.proptit.todohive.data.local.model.TaskWithCategory
import com.proptit.todohive.repository.TaskRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlinx.coroutines.launch

class CalendarViewModel(private val repo: TaskRepository) : ViewModel() {

    private val today = LocalDate.now()

    private val _currentWeekStart = MutableLiveData(
        today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    )
    val currentWeekStart: LiveData<LocalDate> = _currentWeekStart

    val weekDays: LiveData<List<LocalDate>> = currentWeekStart.map { start ->
        (0..6).map { start.plusDays(it.toLong()) }
    }

    private val _selectedDate = MutableLiveData(today)
    val selectedDate: LiveData<LocalDate> = _selectedDate

    private val _showCompleted = MutableLiveData(false)
    val showCompleted: LiveData<Boolean> = _showCompleted

    val monthTitle: LiveData<String> = selectedDate.map { date ->
        val month = date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        "${month.uppercase(Locale.getDefault())} ${date.year}"
    }

    val selectedDateLabel: LiveData<String> = selectedDate.map { date ->
        when {
            date.isEqual(today) -> "Today"
            date.isEqual(today.minusDays(1)) -> "Yesterday"
            date.isEqual(today.plusDays(1)) -> "Tomorrow"
            else -> date.format(DateTimeFormatter.ofPattern("EEE, dd MMM", Locale.getDefault()))
        }
    }

    private val tasksBySelectedDateSrc: LiveData<List<TaskWithCategory>> =
        selectedDate.switchMap { date -> repo.getTasksByDateLive(date) }

    val tasksForSelectedDate: LiveData<List<TaskWithCategory>> =
        MediatorLiveData<List<TaskWithCategory>>().apply {
            fun recompute() {
                val base = tasksBySelectedDateSrc.value.orEmpty()
                val onlyCompleted = _showCompleted.value ?: false
                value = base
                    .filter { if (onlyCompleted) it.task.is_completed else !it.task.is_completed }
                    .sortedBy { it.task.even_at }
            }
            addSource(tasksBySelectedDateSrc) { recompute() }
            addSource(_showCompleted) { recompute() }
        }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        val start = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        if (_currentWeekStart.value != start) _currentWeekStart.value = start
    }

    fun nextWeek() {
        val start = _currentWeekStart.value
            ?: today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        val newStart = start.plusWeeks(1)
        _currentWeekStart.value = newStart
    }

    fun previousWeek() {
        val start = _currentWeekStart.value
            ?: today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        val newStart = start.minusWeeks(1)
        _currentWeekStart.value = newStart
    }

    fun selectToday() {
        val t = LocalDate.now()
        _currentWeekStart.value = t.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        _selectedDate.value = t
    }

    fun setCompletedFilter(isCompleted: Boolean) {
        _showCompleted.value = isCompleted
    }

    fun onToggleDone(task: TaskEntity) = viewModelScope.launch {
        repo.toggleCompleted(task.task_id)
    }

    fun onSwipeDelete(task: TaskEntity) = viewModelScope.launch {
        repo.delete(task.task_id)
    }

    fun restore(task: TaskEntity) = viewModelScope.launch {
        repo.restore(task)
    }

    class Factory(private val repo: TaskRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CalendarViewModel(repo) as T
        }
    }
}