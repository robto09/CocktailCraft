package com.cocktailcraft.util

import java.text.SimpleDateFormat
import java.util.*

actual fun generateUUID(): String = UUID.randomUUID().toString()

actual fun getCurrentTimeMillis(): Long = System.currentTimeMillis()

actual fun formatDate(timestamp: Long, pattern: String): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(timestamp))
}