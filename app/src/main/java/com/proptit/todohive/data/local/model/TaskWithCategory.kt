package com.proptit.todohive.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.proptit.todohive.data.local.entity.CategoryEntity
import com.proptit.todohive.data.local.entity.TaskEntity

data class TaskWithCategory(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "category_id",
        entityColumn = "category_id"
    )
    val category: CategoryEntity?
)