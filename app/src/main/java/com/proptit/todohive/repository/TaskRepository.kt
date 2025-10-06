package com.proptit.todohive.repository

import com.proptit.todohive.data.local.AppDatabase
import com.proptit.todohive.data.local.entity.CategoryEntity
import com.proptit.todohive.data.local.entity.TaskEntity
import com.proptit.todohive.data.local.model.TaskWithCategory
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDate

class TaskRepository(
    private val db: AppDatabase,
    private val currentUserId: Long = 1L
) {
    private val taskDao = db.taskDao()

    fun observeAll(): Flow<List<TaskWithCategory>> = taskDao.observeAllWithCategory()

    fun observeByDayOffset(dayOffset: Int): Flow<List<TaskWithCategory>> =
        taskDao.observeByDayOffset(dayOffset)

    fun observeCompleted(): Flow<List<TaskWithCategory>> = taskDao.observeCompleted()

    fun observeByDayRange(dayOffset: Int): Flow<List<TaskWithCategory>> {
        val zone = ZoneId.systemDefault()
        val day = LocalDate.now(zone).plusDays(dayOffset.toLong())
        val start = day.atStartOfDay(zone).toInstant()
        val end = day.plusDays(1).atStartOfDay(zone).minusNanos(1).toInstant()
        return taskDao.observeByDayRange(start, end)
    }

    fun observeTaskWithCategory(taskId: Long): Flow<TaskWithCategory?> =
        taskDao.observeTaskWithCategory(taskId)

    suspend fun getTaskWithCategoryById(taskId: Long): TaskWithCategory? =
        taskDao.getTaskWithCategoryById(taskId)

    suspend fun getById(id: Long): TaskEntity? = taskDao.getById(id)

    suspend fun getCategoryById(id: Long?): CategoryEntity? =
        id?.let { db.categoryDao().getById(it) }

    suspend fun create(
        title: String,
        at: Instant,
        priority: Int = 1,
        categoryId: Long? = null,
        description: String
    ): Long {
        val task = TaskEntity(
            title = title,
            even_at = at,
            priority = priority,
            is_completed = false,
            user_id = currentUserId,
            category_id = categoryId,
            description = description
        )
        return taskDao.upsert(task)
    }

    suspend fun update(
        id: Long,
        title: String,
        at: Instant,
        priority: Int,
        categoryId: Long?,
        description: String
    ) {
        taskDao.update(
            id,
            title = title,
            evenAt = at,
            priority = priority,
            categoryId = categoryId,
            description = description
        )
    }

    suspend fun setCompleted(id: Long, done: Boolean) = taskDao.setCompleted(id, done)

    suspend fun toggleCompleted(taskId: Long) = taskDao.toggleCompleted(taskId)

    suspend fun delete(id: Long) = taskDao.deleteById(id)
    suspend fun deleteAll() = taskDao.deleteAll()
    suspend fun restore(task: TaskEntity) = taskDao.upsert(task)
}