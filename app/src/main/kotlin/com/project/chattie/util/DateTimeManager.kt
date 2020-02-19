package com.project.chattie.util

import com.project.chattie.ext.toCalendar
import com.project.chattie.ext.toPattern
import java.util.*
import java.util.concurrent.TimeUnit

object DateTimeManager {

    fun formatTime(timeInMillis: Long): String {
        val lastSeenDate = Date(timeInMillis).toCalendar()
        val nowDate = Calendar.getInstance()
        val difference = nowDate.timeInMillis - lastSeenDate.timeInMillis
        return when (TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS).toInt()) {
            0 -> lastSeenDate.toPattern("hh:mm aa")
            1 -> "Yesterday ${lastSeenDate.toPattern("hh:mm aa")}"
            in 2..6 -> lastSeenDate.toPattern("EEE hh:mm aa")
            else -> lastSeenDate.toPattern("dd MMM hh:mm aa")
        }
    }

}