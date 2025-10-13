package com.proptit.todohive.data.local.model

enum class Priority(val level: Int) {
    HIGH(1),
    MEDIUM(2),
    LOW(3);

    companion object {
        fun fromInt(value: Int): Priority =
            values().firstOrNull { it.level == value } ?: MEDIUM
    }
}