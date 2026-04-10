package com.curiosityengine.app.util

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateTimeUtil {

    private val ISO_DATE: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    /** Today's date in the device's local timezone */
    fun today(): LocalDate = LocalDate.now(ZoneId.systemDefault())

    /** Parse an ISO date string (yyyy-MM-dd) to LocalDate */
    fun parseDate(dateStr: String): LocalDate = LocalDate.parse(dateStr, ISO_DATE)

    /** Format a LocalDate to ISO string (yyyy-MM-dd) */
    fun formatDate(date: LocalDate): String = date.format(ISO_DATE)

    /** Returns true if two dates are consecutive calendar days */
    fun areConsecutiveDays(earlier: LocalDate, later: LocalDate): Boolean =
        earlier.plusDays(1) == later

    /** Returns true if the given date is today */
    fun isToday(date: LocalDate): Boolean = date == today()

    /** Returns true if the given date was yesterday */
    fun isYesterday(date: LocalDate): Boolean = date == today().minusDays(1)

    /** Monday of the current week */
    fun currentWeekStart(): LocalDate {
        val today = today()
        return today.minusDays(today.dayOfWeek.value.toLong() - 1)
    }
}
