package com.proptit.todohive.data.local.entity

import androidx.room.*
import java.time.Instant

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns  = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["category_id"],
            childColumns  = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("user_id"), Index("category_id")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "task_id")
    val task_id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "even_at")
    val even_at: Instant,

    @ColumnInfo(name = "priority")
    val priority: Int = 2,

    @ColumnInfo(name = "is_completed", defaultValue = "0")
    val is_completed: Boolean = false,

    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    val is_deleted: Boolean = false,

    @ColumnInfo(name = "deleted_at")
    val deleted_at: Instant? = null,

    @ColumnInfo(name = "user_id")
    val user_id: Long,

    @ColumnInfo(name = "category_id")
    val category_id: Long? = null
)