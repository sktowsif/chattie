package com.project.chattie.ext

import java.text.SimpleDateFormat
import java.util.*

inline fun Calendar.toPattern(pattern: String, locale: Locale = Locale.US): String =
    SimpleDateFormat(pattern, locale).format(time)

inline fun Long.toPattern(pattern: String, locale: Locale = Locale.US): String =
    SimpleDateFormat(pattern, locale).format(Date(this))


inline fun Date.toCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.time = this
    return calendar
}