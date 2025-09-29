package com.proptit.todohive.data.local.dao

import androidx.room.*
import com.proptit.todohive.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(u: UserEntity): Long

    @Query("SELECT * FROM users WHERE user_id = :id")
    suspend fun getById(id: Long): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT user_id FROM users WHERE email = :email LIMIT 1")
    suspend fun getIdByEmail(email: String): Long?
}