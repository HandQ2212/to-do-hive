package com.proptit.todohive.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.proptit.todohive.data.local.AppDatabase
import com.proptit.todohive.data.local.entity.CategoryEntity
import com.proptit.todohive.data.local.entity.TaskEntity
import com.proptit.todohive.data.local.model.TaskWithCategory
import com.proptit.todohive.notification.NotificationScheduler
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class TaskRepository(
    private val appContext: Context,
    private val db: AppDatabase,
    private val currentUserId: Long
) {
    private val taskDao = db.taskDao()
    private val categoryDao = db.categoryDao()

    fun observeAll(): LiveData<List<TaskWithCategory>> =
        taskDao.observeAllWithCategory(currentUserId)

    fun observeByDayOffset(dayOffset: Int): LiveData<List<TaskWithCategory>> =
        taskDao.observeByDayOffset(currentUserId, dayOffset)

    fun observeCompleted(): LiveData<List<TaskWithCategory>> =
        taskDao.observeCompleted(currentUserId)

    fun observeByDayRange(dayOffset: Int): LiveData<List<TaskWithCategory>> {
        val zone = ZoneId.systemDefault()
        val day = LocalDate.now(zone).plusDays(dayOffset.toLong())
        val start = day.atStartOfDay(zone).toInstant()
        val end = day.plusDays(1).atStartOfDay(zone).minusNanos(1).toInstant()
        return taskDao.observeByDayRange(currentUserId, start, end)
    }

    suspend fun getById(id: Long): TaskEntity? =
        taskDao.getById(id, currentUserId)

    suspend fun getCategoryById(id: Long?): CategoryEntity? =
        id?.let { categoryDao.getById(it) }

    private suspend fun ensureCategoryVisible(categoryId: Long?) {
        if (categoryId == null) return
        val cat = categoryDao.getById(categoryId)
            ?: throw IllegalArgumentException("Category does not exist")
        val visible = (cat.owner_user_id == null) || (cat.owner_user_id == currentUserId)
        if (!visible) throw IllegalArgumentException("Category is not visible to current user")
    }

    suspend fun create(
        title: String,
        at: Instant,
        priority: Int = 1,
        categoryId: Long? = null,
        description: String
    ): Long {
        ensureCategoryVisible(categoryId)
        val task = TaskEntity(
            title = title,
            even_at = at,
            priority = priority,
            is_completed = false,
            is_deleted = false,
            deleted_at = null,
            user_id = currentUserId,
            category_id = categoryId,
            description = description
        )
        val id = taskDao.upsert(task)
        NotificationScheduler.scheduleTaskReminder(appContext, task.copy(task_id = id))
        return id
    }

    suspend fun update(
        id: Long,
        title: String,
        at: Instant,
        priority: Int,
        categoryId: Long?,
        description: String
    ) {
        ensureCategoryVisible(categoryId)
        val changed = taskDao.update(
            taskId = id,
            title = title,
            evenAt = at,
            priority = priority,
            categoryId = categoryId,
            description = description,
            userId = currentUserId
        )
        if (changed == 0) throw IllegalStateException("Task not found or not owned by current user.")

        NotificationScheduler.cancelTaskReminder(appContext, id)
        val updated = taskDao.getById(id, currentUserId)
        if (updated != null && !updated.is_deleted && !updated.is_completed) {
            NotificationScheduler.scheduleTaskReminder(appContext, updated)
        }
    }

    suspend fun toggleCompleted(taskId: Long) {
        val changed = taskDao.toggleCompleted(taskId, currentUserId)
        if (changed == 0) throw IllegalStateException("Task not found or not owned by current user.")
        taskDao.getById(taskId, currentUserId)?.let { task ->
            if (task.is_completed) {
                NotificationScheduler.cancelTaskReminder(appContext, taskId)
            } else if (!task.is_deleted) {
                NotificationScheduler.scheduleTaskReminder(appContext, task)
            }
        }
    }

    suspend fun delete(id: Long) {
        moveToBin(id)
    }

    fun observeDeleted(): LiveData<List<TaskWithCategory>> =
        taskDao.observeDeleted(currentUserId)

    suspend fun moveToBin(taskId: Long, userId: Long, deletedAt: Instant) {
        val changed = taskDao.moveToBin(taskId, userId, deletedAt)
        if (changed == 0) throw IllegalStateException("Task not found or not owned by current user.")
        NotificationScheduler.cancelTaskReminder(appContext, taskId)
    }

    suspend fun moveToBin(taskId: Long) {
        moveToBin(taskId, currentUserId, Instant.now())
    }

    suspend fun restoreFromBin(taskId: Long) {
        val changed = taskDao.restoreFromBin(taskId, currentUserId)
        if (changed == 0) throw IllegalStateException("Task not found or not owned by current user.")
        taskDao.getById(taskId, currentUserId)?.let { task ->
            if (!task.is_completed && !task.is_deleted) {
                NotificationScheduler.scheduleTaskReminder(appContext, task)
            }
        }
    }

    suspend fun deleteForever(taskId: Long) {
        val changed = taskDao.deleteForever(taskId, currentUserId)
        if (changed == 0) throw IllegalStateException("Task not found or not owned by current user.")
        NotificationScheduler.cancelTaskReminder(appContext, taskId)
    }

    suspend fun restore(task: TaskEntity): Long {
        if (task.user_id != currentUserId) {
            throw IllegalArgumentException("Cannot restore task of another user.")
        }
        val restoredId = taskDao.upsert(task.copy(is_deleted = false, deleted_at = null))
        NotificationScheduler.scheduleTaskReminder(appContext, task.copy(task_id = restoredId, is_deleted = false, deleted_at = null))
        return restoredId
    }

    fun observeByDayRange(start: Instant, end: Instant): LiveData<List<TaskWithCategory>> {
        return taskDao.observeByDayRange(currentUserId, start, end)
    }

    suspend fun restoreAll() {
        taskDao.restoreAll()
    }

    suspend fun clearAll() {
        taskDao.clearAll()
    }

    fun getTasksByDateLive(date: LocalDate): LiveData<List<TaskWithCategory>> {
        val zone = ZoneId.systemDefault()
        val startMillis = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMillis = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return taskDao.getTasksByDateRangeLive(currentUserId, startMillis, endMillis)
    }
}