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
    @PrimaryKey(autoGenerate = true) val task_id: Long = 0,
    val title: String,
    val description: String? = null,
    val even_at: Instant,
    val priority: Int = 2,
    @ColumnInfo(defaultValue = "0") val is_completed: Boolean = false,
    val user_id: Long,
    val category_id: Long? = null
)