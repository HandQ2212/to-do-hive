package com.proptit.todohive.repository

import com.proptit.todohive.data.local.AppDatabase
import com.proptit.todohive.data.local.entity.TaskEntity
import com.proptit.todohive.data.local.model.TaskWithCategory
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Repository cho Task: trung gian giữa UI/ViewModel và Room.
 * - currentUserId: id user hiện tại (tùy cách bạn quản lý đăng nhập)
 */
class TaskRepository(
    private val db: AppDatabase,
    private val currentUserId: Long = 1L
) {
    private val dao = db.taskDao()

    fun observeAll(): Flow<List<TaskWithCategory>> = dao.observeAllWithCategory()

    fun observeByDayOffset(dayOffset: Int): Flow<List<TaskWithCategory>> =
        dao.observeByDayOffset(dayOffset)

    fun observeCompleted(): Flow<List<TaskWithCategory>> = dao.observeCompleted()

    suspend fun getById(id: Long): TaskEntity? = dao.getById(id)

    suspend fun create(
        title: String,
        at: Instant,
        priority: Int = 1,
        categoryId: Long? = null
    ): Long {
        val task = TaskEntity(
            title = title,
            even_at = at,
            priority = priority,
            is_completed = false,
            user_id = currentUserId,
            category_id = categoryId
        )
        return dao.upsert(task)
    }

    suspend fun update(
        id: Long,
        title: String,
        at: Instant,
        priority: Int,
        categoryId: Long?
    ) {
        dao.update(id, title, at, priority, categoryId)
    }

    suspend fun setCompleted(id: Long, done: Boolean) = dao.setCompleted(id, done)

    suspend fun toggleCompleted(id: Long) {
        val cur = dao.getById(id) ?: return
        dao.setCompleted(id, !cur.is_completed)
    }

    suspend fun delete(id: Long) = dao.deleteById(id)
    suspend fun deleteAll() = dao.deleteAll()

    fun observeByDayRange(dayOffset: Int): Flow<List<TaskWithCategory>> {
        val zone = java.time.ZoneId.systemDefault()
        val day  = java.time.LocalDate.now(zone).plusDays(dayOffset.toLong())
        val start = day.atStartOfDay(zone).toInstant()
        val end   = day.plusDays(1).atStartOfDay(zone).minusNanos(1).toInstant()
        return dao.observeByDayRange(start, end)
    }
}