package com.proptit.todohive.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.proptit.todohive.data.local.entity.CategoryEntity

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(c: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertStrict(c: CategoryEntity): Long

    @Update
    suspend fun update(c: CategoryEntity): Int

    @Query("""
        UPDATE categories 
        SET name = :newName 
        WHERE category_id = :id AND owner_user_id = :ownerUserId
    """)
    suspend fun renameOwned(id: Long, newName: String, ownerUserId: Long): Int

    @Query("""
        UPDATE categories 
        SET icon = :icon, color_hex = :colorHex 
        WHERE category_id = :id AND owner_user_id = :ownerUserId
    """)
    suspend fun updateStyleOwned(id: Long, icon: String?, colorHex: String, ownerUserId: Long): Int

    @Query("""
        DELETE FROM categories 
        WHERE category_id = :id AND owner_user_id = :ownerUserId
    """)
    suspend fun deleteOwnedById(id: Long, ownerUserId: Long): Int

    @Query("SELECT * FROM categories ORDER BY name")
    fun observeAll(): LiveData<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE category_id = :id LIMIT 1")
    suspend fun getById(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories WHERE category_id = :id LIMIT 1")
    fun observeById(id: Long): LiveData<CategoryEntity?>

    @Query("""
        SELECT * FROM categories
        WHERE owner_user_id IS NULL OR owner_user_id = :userId
        ORDER BY 
            CASE WHEN owner_user_id IS NULL THEN 0 ELSE 1 END,
            name
    """)
    fun observeVisibleForUser(userId: Long): LiveData<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE owner_user_id IS NULL ORDER BY name")
    fun observeGlobal(): LiveData<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE owner_user_id = :userId ORDER BY name")
    fun observePersonal(userId: Long): LiveData<List<CategoryEntity>>

    @Query("""
        SELECT * FROM categories
        WHERE name = :name
          AND (
                (:ownerUserId IS NULL AND owner_user_id IS NULL)
             OR (owner_user_id = :ownerUserId)
          )
        LIMIT 1
    """)
    suspend fun getByNameInScope(name: String, ownerUserId: Long?): CategoryEntity?

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM categories
            WHERE name = :name
              AND (
                    (:ownerUserId IS NULL AND owner_user_id IS NULL)
                 OR (owner_user_id = :ownerUserId)
                  )
              AND (:excludeId IS NULL OR category_id <> :excludeId)
        )
    """)
    suspend fun nameExistsInScope(
        name: String,
        ownerUserId: Long?,
        excludeId: Long? = null
    ): Boolean

    @Query("""
        SELECT COUNT(*) FROM categories
        WHERE owner_user_id IS NULL OR owner_user_id = :userId
    """)
    fun countVisibleForUser(userId: Long): LiveData<Int>
}