package com.project.chattie.ext

import java.text.SimpleDateFormat
import java.util.*

inline fun Long.toPattern(pattern: String, locale: Locale = Locale.US) =
    SimpleDateFormat(pattern, locale).format(Date(this))