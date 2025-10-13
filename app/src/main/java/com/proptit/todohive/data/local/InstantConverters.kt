package com.proptit.todohive.data.local

import androidx.room.TypeConverter
import java.time.Instant

class InstantConverters {
    @TypeConverter fun fromEpoch(millis: Long?): Instant? = millis?.let { Instant.ofEpochMilli(it) }
    @TypeConverter fun toEpoch(instant: Instant?): Long? = instant?.toEpochMilli()
}