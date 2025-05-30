package com.cocktailcraft.domain.model

import kotlinx.serialization.Serializable
import com.cocktailcraft.util.getCurrentTimeMillis
import com.cocktailcraft.util.formatDate

@Serializable
data class Review(
    val id: String = getCurrentTimeMillis().toString(),
    val cocktailId: String,
    val userName: String,
    val rating: Float,
    val comment: String,
    val date: String = formatDate(getCurrentTimeMillis(), "yyyy-MM-dd")
) 