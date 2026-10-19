package com.cocktailcraft.domain.model

import com.cocktailcraft.util.UUID
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.native.HiddenFromObjC
import kotlin.time.Clock

@Serializable
data class Review(
    // UUID, not a timestamp: ids drive update/delete, so they must never collide
    val id: String = UUID.randomUUID(),
    val cocktailId: String,
    val userName: String,
    val rating: Float,
    val comment: String,
    val date: String = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        .let { "${it.year}-${it.month.number.toString().padStart(2, '0')}-${it.day.toString().padStart(2, '0')}" },
    // Whether whoever is reading (the signed-in user, or the guest) wrote it:
    // everyone on the device reads every review, but only its author may
    // edit or delete it. Relative to the reader, so never serialized.
    @Transient
    val isMine: Boolean = true
) {
    // Hidden so the generated serializer does not drag the
    // kotlinx-serialization type tree into the Obj-C header.
    @HiddenFromObjC
    companion object
}