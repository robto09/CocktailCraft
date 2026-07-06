package com.cocktailcraft.domain.model

/**
 * Aggregate order statistics, typed so the Swift surface gets real fields
 * instead of a [String: Any] dictionary.
 */
data class OrderStatistics(
    val totalOrders: Int,
    val totalSpent: Double,
    val averageOrderValue: Double,
    val statusBreakdown: Map<String, Int>
)
