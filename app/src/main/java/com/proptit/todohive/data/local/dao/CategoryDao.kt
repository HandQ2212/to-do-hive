package com.proptit.todohive.data.local.dao

import androidx.room.*
import com.proptit.todohive.data.local.entity.CategoryEntity

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(c: CategoryEntity): Long
    @Query("SELECT * FROM categories ORDER BY name") suspend fun getAll(): List<CategoryEntity>
}