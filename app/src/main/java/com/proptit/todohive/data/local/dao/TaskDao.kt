package com.proptit.todohive.data.local.dao

import androidx.room.*
import com.proptit.todohive.data.local.entity.TaskEntity
import com.proptit.todohive.data.local.model.TaskWithCategory
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tasks: List<TaskEntity>): List<Long>

    @Query("SELECT * FROM tasks WHERE task_id = :id LIMIT 1")
    suspend fun getById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks ORDER BY even_at ASC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Transaction
    @Query("""
        SELECT * FROM tasks 
        ORDER BY is_completed ASC, even_at ASC
    """)
    fun observeAllWithCategory(): Flow<List<TaskWithCategory>>

    @Transaction
    @Query("""
        SELECT * FROM tasks
        WHERE date(even_at/1000, 'unixepoch', 'localtime') =
              date(strftime('%s','now','localtime') * 1000 + :dayOffset*24*60*60*1000, 'unixepoch', 'localtime')
        ORDER BY even_at ASC
    """)
    fun observeByDayOffset(dayOffset: Int): Flow<List<TaskWithCategory>>

    @Transaction
    @Query("""
        SELECT * FROM tasks
        WHERE is_completed = 1
        ORDER BY even_at DESC
    """)
    fun observeCompleted(): Flow<List<TaskWithCategory>>

    @Query("UPDATE tasks SET title = :title, even_at = :evenAt, priority = :priority, category_id = :categoryId WHERE task_id = :taskId")
    suspend fun update(taskId: Long, title: String, evenAt: Instant, priority: Int, categoryId: Long?)

    @Query("UPDATE tasks SET is_completed = :done WHERE task_id = :taskId")
    suspend fun setCompleted(taskId: Long, done: Boolean)

    @Query("DELETE FROM tasks WHERE task_id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()

    @Transaction
    @Query("""
        SELECT * FROM tasks
        WHERE even_at BETWEEN :start AND :end
        ORDER BY even_at ASC
    """)
    fun observeByDayRange(
        start: Instant,
        end: Instant
    ): Flow<List<TaskWithCategory>>
}