package com.proptit.todohive.data.local.model

import java.time.Instant

data class Task(
    val taskId: Long = 0,
    val title: String,
    val description: String? = null,
    val eventAt: Instant,
    val priority: Priority = Priority.MEDIUM,
    val isCompleted: Boolean = false,
    val userId: Long,
    val categoryId: Long? = null
)