package com.proptit.todohive.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["name", "owner_user_id"], unique = true),
        Index(value = ["owner_user_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["owner_user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "category_id")
    val category_id: Long = 0,

    @ColumnInfo(name = "owner_user_id")
    val owner_user_id: Long? = null,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "icon")
    val icon: String? = null,

    @ColumnInfo(name = "color_hex")
    val color_hex: String = "#6C63FF",

    @ColumnInfo(name = "created_at")
    val created_at: Instant = Instant.now()
)