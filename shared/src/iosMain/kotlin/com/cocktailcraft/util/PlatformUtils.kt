package com.cocktailcraft.util

import platform.Foundation.*

actual fun generateUUID(): String = NSUUID().UUIDString()

actual fun getCurrentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

actual fun formatDate(timestamp: Long, pattern: String): String {
    val date = NSDate.dateWithTimeIntervalSince1970(timestamp / 1000.0)
    val formatter = NSDateFormatter()
    formatter.dateFormat = pattern
    formatter.locale = NSLocale.currentLocale
    return formatter.stringFromDate(date)
}