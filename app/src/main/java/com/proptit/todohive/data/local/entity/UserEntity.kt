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
    @PrimaryKey(autoGenerate = true) val user_id: Long = 0,
    @ColumnInfo(collate = ColumnInfo.Companion.NOCASE) val username: String,
    val password_hash: String,
    val email: String,
    val created_at: Instant = Instant.now(),
    val updated_at: Instant = Instant.now()
)