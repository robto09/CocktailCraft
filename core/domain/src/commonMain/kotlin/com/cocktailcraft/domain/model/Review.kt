package com.cocktailcraft.domain.model

import com.cocktailcraft.util.UUID
import kotlin.native.HiddenFromObjC
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

@Serializable
data class Review(
    // UUID, not a timestamp: ids drive update/delete, so they must never collide
    val id: String = UUID.randomUUID(),
    val cocktailId: String,
    val userName: String,
    val rating: Float,
    val comment: String,
    val date: String = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        .let { "${it.year}-${it.month.number.toString().padStart(2, '0')}-${it.day.toString().padStart(2, '0')}" }
) {
    // Hidden so the generated serializer does not drag the
    // kotlinx-serialization type tree into the Obj-C header.
    @HiddenFromObjC
    companion object
}