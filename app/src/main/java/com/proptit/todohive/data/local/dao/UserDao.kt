package com.proptit.todohive.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.proptit.todohive.data.local.entity.UserEntity
import java.time.Instant

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(u: UserEntity): Long

    @Query("SELECT * FROM users WHERE user_id = :id LIMIT 1")
    suspend fun getById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE user_id = :id LIMIT 1")
    fun observeById(id: Long): LiveData<UserEntity?>

    @Query("SELECT * FROM users ORDER BY created_at DESC")
    fun observeAll(): LiveData<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT user_id FROM users WHERE email = :email LIMIT 1")
    suspend fun getIdByEmail(email: String): Long?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): UserEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username)")
    suspend fun existsByUsername(username: String): Boolean

    @Query("UPDATE users SET username = :name, updated_at = :updatedAt WHERE user_id = :id")
    suspend fun updateNameById(id: Long, name: String, updatedAt: Instant): Int

    @Query("UPDATE users SET avatar_url = :url, updated_at = :updatedAt WHERE user_id = :id")
    suspend fun updateAvatarById(id: Long, url: String?, updatedAt: Instant): Int

    @Query("UPDATE users SET password_hash = :hash, updated_at = :updatedAt WHERE user_id = :id")
    suspend fun updatePasswordHashById(id: Long, hash: String, updatedAt: Instant): Int
}