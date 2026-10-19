package com.cocktailcraft.data.local

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// Who owns the user's data: the cart, favorites, recently viewed list,
// orders and reviews. Every row of those tables has an owner_id, and every
// read and write is filtered by the current owner, except that reviews are
// read unfiltered:
//
// - Signed in, the owner is the user's id; signed out, it is OwnerIds.GUEST.
//   Signing out or switching accounts moves nothing: the next owner sees
//   only their own data, and the previous user's is back when they sign in.
// - Reviews are the exception: everyone on the device (every user and the
//   guest) reads every review, so a cocktail's ratings don't depend on who
//   is signed in. Only a review's owner edits or deletes it (Review.isMine
//   marks the reader's own).
// - Signing in (or up) from the guest merges the guest's cart, favorites
//   and recently viewed list into the account (mergeGuestUserData), the way
//   a shop keeps the cart you built before logging in: quantities add up,
//   favorites and views are combined. They are the guest's work in progress
//   on this device, and leaving them with a guest nobody returns to would
//   lose them. Orders and reviews stay with the guest: they were placed and
//   written under that identity (a review shows its author's name), and the
//   guest still sees, and alone edits, them after signing out.
// - Rows written before owners existed (database version 6 and earlier,
//   or the Settings data of older versions) are assigned on the first open
//   after the upgrade to whoever is signed in then, else the guest
//   (claimUnassignedUserData). Nothing is dropped.

/** The owner_id values that aren't a user id. */
internal object OwnerIds {
    /** Owner of the data used while signed out. User ids are UUIDs, so it can't clash with one. */
    const val GUEST = "guest"

    /**
     * Rows not assigned yet: what migrations/6.sqm gives existing rows, and
     * what the legacy Settings imports write.
     */
    const val UNASSIGNED = ""
}

/** Whose user data reads and writes go to. */
internal interface CurrentOwner {
    /** The current owner: a user id, or [OwnerIds.GUEST]. */
    suspend fun ownerId(): String

    /** [ownerId], and again whenever it changes (sign-in, sign-out). */
    val ownerIds: Flow<String>
}

/** An owner that never changes; the default of the test-facing constructors. */
internal class FixedOwner(private val id: String = OwnerIds.GUEST) : CurrentOwner {
    override suspend fun ownerId(): String = id

    override val ownerIds: Flow<String> = flowOf(id)
}

/**
 * Assigns every unassigned row ([OwnerIds.UNASSIGNED]) to [ownerId]. Runs on
 * every database open; only the first open after an upgrade (or a legacy
 * import) finds anything to assign.
 */
internal fun CocktailCraftDatabase.claimUnassignedUserData(ownerId: String) {
    transaction {
        cartItemQueries.reassign(toOwnerId = ownerId, fromOwnerId = OwnerIds.UNASSIGNED)
        favoriteCocktailQueries.reassign(toOwnerId = ownerId, fromOwnerId = OwnerIds.UNASSIGNED)
        recentlyViewedCocktailQueries.reassign(toOwnerId = ownerId, fromOwnerId = OwnerIds.UNASSIGNED)
        customerOrderQueries.reassign(toOwnerId = ownerId, fromOwnerId = OwnerIds.UNASSIGNED)
        cocktailReviewQueries.reassign(toOwnerId = ownerId, fromOwnerId = OwnerIds.UNASSIGNED)
    }
}

/**
 * Moves the guest's cart, favorites and recently viewed list into
 * [accountId]'s, in one transaction: a cocktail in both carts gets the
 * quantities added up (keeping the account line's price and place), the
 * guest's other lines follow the account's, favorites and views are
 * combined (the later view of a cocktail viewed by both wins, and the list
 * keeps its cap). Orders and reviews stay with the guest.
 */
internal fun CocktailCraftDatabase.mergeGuestUserData(accountId: String) {
    transaction {
        cartItemQueries.addQuantitiesOf(fromOwnerId = OwnerIds.GUEST, toOwnerId = accountId)
        cartItemQueries.reassign(toOwnerId = accountId, fromOwnerId = OwnerIds.GUEST)
        cartItemQueries.deleteAll(OwnerIds.GUEST)

        favoriteCocktailQueries.reassign(toOwnerId = accountId, fromOwnerId = OwnerIds.GUEST)
        favoriteCocktailQueries.deleteAll(OwnerIds.GUEST)

        recentlyViewedCocktailQueries.keepLatestViewsOf(fromOwnerId = OwnerIds.GUEST, toOwnerId = accountId)
        recentlyViewedCocktailQueries.reassign(toOwnerId = accountId, fromOwnerId = OwnerIds.GUEST)
        recentlyViewedCocktailQueries.deleteAll(OwnerIds.GUEST)
        recentlyViewedCocktailQueries.trimToCapacity(
            ownerId = accountId,
            capacity = CocktailCache.MAX_RECENTLY_VIEWED.toLong()
        )
    }
}
