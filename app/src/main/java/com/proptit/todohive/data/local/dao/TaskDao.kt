package com.proptit.todohive.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.proptit.todohive.data.local.entity.TaskEntity
import com.proptit.todohive.data.local.model.TaskWithCategory
import java.time.Instant

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tasks: List<TaskEntity>): List<Long>

    @Query("""
        UPDATE tasks 
        SET title = :title, even_at = :evenAt, priority = :priority, category_id = :categoryId, description = :description
        WHERE task_id = :taskId AND user_id = :userId
    """)
    suspend fun update(
        taskId: Long,
        title: String,
        evenAt: Instant,
        priority: Int,
        categoryId: Long?,
        description: String,
        userId: Long
    ): Int

    @Query("UPDATE tasks SET is_completed = :done WHERE task_id = :taskId AND user_id = :userId")
    suspend fun setCompleted(taskId: Long, done: Boolean, userId: Long): Int

    @Query("UPDATE tasks SET is_completed = NOT is_completed WHERE task_id = :taskId AND user_id = :userId")
    suspend fun toggleCompleted(taskId: Long, userId: Long): Int

    @Query("DELETE FROM tasks WHERE task_id = :id AND user_id = :userId")
    suspend fun deleteById(id: Long, userId: Long): Int

    @Query("DELETE FROM tasks WHERE user_id = :userId")
    suspend fun deleteAll(userId: Long): Int

    @Query("SELECT * FROM tasks WHERE task_id = :id AND user_id = :userId LIMIT 1")
    suspend fun getById(id: Long, userId: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE user_id = :userId AND is_deleted = 0 ORDER BY even_at ASC")
    fun observeAll(userId: Long): LiveData<List<TaskEntity>>

    @Transaction
    @Query("""
        SELECT * FROM tasks 
        WHERE user_id = :userId AND is_deleted = 0
        ORDER BY is_completed ASC, even_at ASC
    """)
    fun observeAllWithCategory(userId: Long): LiveData<List<TaskWithCategory>>

    @Transaction
    @Query("""
        SELECT * FROM tasks
        WHERE user_id = :userId
          AND is_deleted = 0
          AND date(even_at/1000, 'unixepoch', 'localtime') =
              date(strftime('%s','now','localtime') + :dayOffset*24*60*60, 'unixepoch', 'localtime')
        ORDER BY even_at ASC
    """)
    fun observeByDayOffset(userId: Long, dayOffset: Int): LiveData<List<TaskWithCategory>>

    @Transaction
    @Query("""
        SELECT * FROM tasks
        WHERE user_id = :userId
          AND is_deleted = 0
          AND is_completed = 1
        ORDER BY even_at DESC
    """)
    fun observeCompleted(userId: Long): LiveData<List<TaskWithCategory>>

    @Transaction
    @Query("""
        SELECT * FROM tasks
        WHERE user_id = :userId
          AND is_deleted = 0
          AND even_at BETWEEN :start AND :end
        ORDER BY even_at ASC
    """)
    fun observeByDayRange(
        userId: Long,
        start: Instant,
        end: Instant
    ): LiveData<List<TaskWithCategory>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE task_id = :id AND user_id = :userId LIMIT 1")
    fun observeTaskWithCategory(id: Long, userId: Long): LiveData<TaskWithCategory?>

    @Transaction
    @Query("SELECT * FROM tasks WHERE task_id = :id AND user_id = :userId LIMIT 1")
    suspend fun getTaskWithCategoryById(id: Long, userId: Long): TaskWithCategory?

    @Query("UPDATE tasks SET is_deleted = 1, deleted_at = :deletedAt WHERE task_id = :taskId AND user_id = :userId")
    suspend fun moveToBin(taskId: Long, userId: Long, deletedAt: Instant): Int

    @Query("UPDATE tasks SET is_deleted = 0, deleted_at = NULL WHERE task_id = :taskId AND user_id = :userId")
    suspend fun restoreFromBin(taskId: Long, userId: Long): Int

    @Transaction
    @Query("""
        SELECT * FROM tasks
        WHERE user_id = :userId
          AND is_deleted = 1
        ORDER BY deleted_at DESC
    """)
    fun observeDeleted(userId: Long): LiveData<List<TaskWithCategory>>

    @Query("DELETE FROM tasks WHERE task_id = :taskId AND user_id = :userId")
    suspend fun deleteForever(taskId: Long, userId: Long): Int

    @Query("UPDATE tasks SET is_deleted = 0 WHERE is_deleted = 1")
    suspend fun restoreAll()

    @Query("DELETE FROM tasks WHERE is_deleted = 1")
    suspend fun clearAll()

    @Query("""
        SELECT * FROM tasks
        WHERE user_id = :userId 
          AND is_deleted = 0 
          AND even_at >= :start 
          AND even_at < :end
        ORDER BY even_at ASC
    """)
    fun getTasksByDateRangeLive(
        userId: Long,
        start: Long,
        end: Long
    ): LiveData<List<TaskWithCategory>>
}