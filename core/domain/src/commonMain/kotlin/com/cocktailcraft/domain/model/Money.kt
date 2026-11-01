package com.cocktailcraft.domain.model

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
 * typed `Money` with [cents] and the arithmetic.
 */
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

    // Kotlin-side constants; Swift spells amounts as Money(cents:)
    @HiddenFromObjC
    companion object {
        val ZERO = Money(0)
    }
}

/** The sum of [selector] over the elements, [Money.ZERO] when there are none. */
@HiddenFromObjC
inline fun <T> Iterable<T>.sumOfMoney(selector: (T) -> Money): Money {
    var cents = 0L
    for (element in this) cents += selector(element).cents
    return Money(cents)
}
