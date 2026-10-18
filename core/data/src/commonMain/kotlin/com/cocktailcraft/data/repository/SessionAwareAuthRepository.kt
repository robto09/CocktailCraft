package com.cocktailcraft.data.repository

import co.touchlab.kermit.Logger
import com.cocktailcraft.data.local.LocalDatabase
import com.cocktailcraft.data.local.OwnerIds
import com.cocktailcraft.data.local.UserSession
import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.runCatchingNonCancellation
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The app's [AuthRepository]: [delegate] (the account store) plus keeping
 * the owner of the user's data in step with who is signed in. After a
 * successful sign-in, sign-up or sign-out it switches [session] to the new
 * owner, so the cart, favorites, recently viewed list, orders and reviews
 * everywhere show that owner's rows; moving from the guest to an account
 * first merges the guest's cart, favorites and recently viewed list into it
 * (the rules are in UserDataOwnership.kt).
 */
internal class SessionAwareAuthRepository(
    private val delegate: AuthRepository,
    private val session: UserSession,
    private val localDatabase: LocalDatabase
) : AuthRepository by delegate {

    // One owner change at a time: a merge must finish before the next starts
    private val switchMutex = Mutex()

    override suspend fun signUp(email: String, password: String): Result<Boolean> =
        delegate.signUp(email, password).also { if (it.succeeded) ownerMayHaveChanged() }

    override suspend fun signIn(email: String, password: String): Result<Boolean> =
        delegate.signIn(email, password).also { if (it.succeeded) ownerMayHaveChanged() }

    override suspend fun signOut(): Result<Boolean> =
        delegate.signOut().also { if (it.succeeded) ownerMayHaveChanged() }

    private val Result<Boolean>.succeeded: Boolean
        get() = this is Result.Success && data

    private suspend fun ownerMayHaveChanged() = switchMutex.withLock {
        val previous = session.ownerId()
        val next = session.signedInOwner()
        if (next == previous) return@withLock
        if (previous == OwnerIds.GUEST) {
            // A failed merge leaves the guest's data with the guest, where
            // signing out shows it again: nothing is lost
            runCatchingNonCancellation(onFailure = { e -> Logger.e(e) { "Merging guest data failed: ${e.message}" } }) {
                localDatabase.mergeGuestInto(next)
            }
        }
        session.switchTo(next)
    }
}
