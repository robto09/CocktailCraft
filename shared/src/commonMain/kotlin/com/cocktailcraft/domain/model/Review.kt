package com.cocktailcraft.domain.model

import kotlin.native.HiddenFromObjC
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Serializable
data class Review(
    val id: String = Clock.System.now().toEpochMilliseconds().toString(),
    val cocktailId: String,
    val userName: String,
    val rating: Float,
    val comment: String,
    val date: String = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        .let { "${it.year}-${it.monthNumber.toString().padStart(2, '0')}-${it.dayOfMonth.toString().padStart(2, '0')}" }
) {
    // Hidden so the generated serializer does not drag the
    // kotlinx-serialization type tree into the Obj-C header.
    @HiddenFromObjC
    companion object
}