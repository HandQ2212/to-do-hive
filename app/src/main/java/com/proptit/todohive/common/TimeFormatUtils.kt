package com.proptit.todohive.common

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TimeFormatUtils {
    private val timeFormatter = DateTimeFormatter.ofPattern("EEE, HH:mm")

    fun formatInstantShort(instant: Instant): String =
        timeFormatter.format(instant.atZone(ZoneId.systemDefault()))
}