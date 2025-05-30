package com.cocktailcraft.util

expect fun generateUUID(): String

expect fun getCurrentTimeMillis(): Long

expect fun formatDate(timestamp: Long, pattern: String): String