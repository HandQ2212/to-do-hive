package com.proptit.todohive.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["email"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    val user_id: Long = 0,

    @ColumnInfo(name = "username", collate = ColumnInfo.Companion.NOCASE)
    val username: String,

    @ColumnInfo(name = "password_hash")
    val password_hash: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "created_at")
    val created_at: Instant = Instant.now(),

    @ColumnInfo(name = "updated_at")
    val updated_at: Instant = Instant.now()
)