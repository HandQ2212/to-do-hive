package com.proptit.todohive.ui.home.task

import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

object TimeFmt {
    private val locale: Locale = Locale.getDefault()

    private val DATE_FMT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEE, MMM d", locale) // Mon, Sep 29

    private val TIME_FMT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm", locale) // 14:05

    private val FULL_FMT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEE, MMM d • HH:mm", locale)

    fun date(d: LocalDate): String = d.format(DATE_FMT)
    fun time(t: LocalTime): String = t.truncatedTo(java.time.temporal.ChronoUnit.MINUTES).format(TIME_FMT)
    fun full(zdt: ZonedDateTime): String = zdt.truncatedTo(java.time.temporal.ChronoUnit.MINUTES).format(FULL_FMT)
}
