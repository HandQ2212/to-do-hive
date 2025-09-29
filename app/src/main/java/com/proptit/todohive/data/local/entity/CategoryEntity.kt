package com.proptit.todohive.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val category_id: Long = 0,
    val name: String,
    val icon: String? = null,
    val color_hex: String = "#6C63FF",
    val created_at: Instant = Instant.now()
)