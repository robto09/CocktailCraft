package com.cocktailcraft.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val id: String = System.currentTimeMillis().toString(),
    val cocktailId: String,
    val userName: String,
    val rating: Float,
    val comment: String,
    val date: String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        .format(java.util.Date())
) 