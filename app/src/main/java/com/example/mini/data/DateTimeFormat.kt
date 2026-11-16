// #member1
// Date/time text formats and day/hour helpers used across the app.
package com.example.mini.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

const val HOUR_MILLIS = 60 * 60 * 1000L
const val DAY_MILLIS = 24 * HOUR_MILLIS

private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
private val shortDateFormat = SimpleDateFormat("EEE\ndd MMM", Locale.getDefault())
private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

fun formatDateTime(millis: Long): String = dateTimeFormat.format(Date(millis))

fun formatDate(millis: Long): String = dateFormat.format(Date(millis))

// "Mon\n17 Nov", for date chips
fun formatShortDate(millis: Long): String = shortDateFormat.format(Date(millis))

fun formatTime(millis: Long): String = timeFormat.format(Date(millis))

// 9 -> "09:00"
fun formatHour(hour: Int): String = "%02d:00".format(hour)

// ---------- day / hour helpers (local time) ----------

fun startOfDay(millis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = millis
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

fun hourOf(millis: Long): Int =
    Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.HOUR_OF_DAY)

fun atHour(dayMillis: Long, hour: Int): Long = Calendar.getInstance().apply {
    timeInMillis = startOfDay(dayMillis)
    set(Calendar.HOUR_OF_DAY, hour)
}.timeInMillis

// adds whole days without DST surprises
fun addDays(dayMillis: Long, days: Int): Long = Calendar.getInstance().apply {
    timeInMillis = startOfDay(dayMillis)
    add(Calendar.DAY_OF_MONTH, days)
}.timeInMillis

// ---------- DatePicker conversions ----------
// The Material DatePicker works in UTC midnight, the rest of the app in local time.

fun localDateToUtc(localMillis: Long): Long {
    val local = Calendar.getInstance().apply { timeInMillis = localMillis }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

fun utcToLocalDate(utcMillis: Long): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
    return Calendar.getInstance().apply {
        clear()
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}
