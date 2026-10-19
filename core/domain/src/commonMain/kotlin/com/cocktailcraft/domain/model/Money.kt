package com.cocktailcraft.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.native.HiddenFromObjC

/**
 * An amount of US dollars, held as whole cents so prices, totals and fees
 * add up exactly (no floating-point drift). USD is the only currency the
 * app sells in; :presentation's Money.formatted() is the one place an
 * amount becomes text.
 *
 * A regular class rather than a value class on purpose: a Kotlin value
 * class reaches Swift as a bare Int64 (and as an untyped `Any?` when
 * nullable) with none of its members exported, while this one stays a
 * typed `Money` with [cents], the arithmetic and the formatter.
 *
 * Serialized (database payloads) as the plain number of cents.
 */
@Serializable(with = MoneyCentsSerializer::class)
class Money(val cents: Long) : Comparable<Money> {

    operator fun plus(other: Money): Money = Money(cents + other.cents)

    operator fun minus(other: Money): Money = Money(cents - other.cents)

    /** This amount [quantity] times, e.g. a unit price times the units ordered. */
    operator fun times(quantity: Int): Money = Money(cents * quantity)

    override fun compareTo(other: Money): Int = cents.compareTo(other.cents)

    fun coerceAtLeast(minimum: Money): Money = if (this < minimum) minimum else this

    override fun equals(other: Any?): Boolean = other is Money && other.cents == cents

    override fun hashCode(): Int = cents.hashCode()

    override fun toString(): String = "Money(cents=$cents)"

    // Hidden so the generated serializer() accessor does not drag the
    // kotlinx-serialization type tree into the Obj-C header. Swift spells
    // amounts as Money(cents:).
    @HiddenFromObjC
    companion object {
        val ZERO = Money(0)

        private const val CENTS_PER_DOLLAR = 100

        /**
         * Converts a legacy floating-point dollar amount (what the app stored
         * before money was kept in cents) to cents, rounding half away from
         * zero exactly like SQLite's ROUND() in migrations/4.sqm, so a price
         * read from an old JSON payload matches the same price migrated in
         * a database column.
         */
        internal fun fromDollars(dollars: Double): Money {
            val scaled = dollars * CENTS_PER_DOLLAR
            return Money((scaled + if (scaled < 0) -HALF else HALF).toLong())
        }

        private const val HALF = 0.5
    }
}

/** The sum of [selector] over the elements, [Money.ZERO] when there are none. */
@HiddenFromObjC
inline fun <T> Iterable<T>.sumOfMoney(selector: (T) -> Money): Money {
    var cents = 0L
    for (element in this) cents += selector(element).cents
    return Money(cents)
}

/** [Money] as its number of cents. */
internal object MoneyCentsSerializer : KSerializer<Money> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("com.cocktailcraft.domain.model.Money", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Money) = encoder.encodeLong(value.cents)

    override fun deserialize(decoder: Decoder): Money = Money(decoder.decodeLong())
}
