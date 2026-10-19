package com.cocktailcraft.data.local

import com.cocktailcraft.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The [CurrentOwner] of the app: whoever the auth store says is signed in,
 * else [OwnerIds.GUEST] (see UserDataOwnership.kt).
 *
 * Reads the auth store once, lazily, through [authRepository]'s existing
 * getCurrentUser; after that the owner only changes through [switchTo],
 * which the auth repository wrapper calls after a sign-in, sign-up or
 * sign-out (see SessionAwareAuthRepository). Observers of the user's data
 * follow [ownerIds], so they switch to the new owner's rows at once.
 */
internal class UserSession(private val authRepository: AuthRepository) : CurrentOwner {

    private val current = MutableStateFlow<String?>(null)
    private val loadMutex = Mutex()

    override suspend fun ownerId(): String =
        current.value ?: loadMutex.withLock {
            current.value ?: signedInOwner().also { current.value = it }
        }

    override val ownerIds: Flow<String> =
        flow {
            ownerId()
            emitAll(current.filterNotNull())
        }.distinctUntilChanged()

    /** Who the auth store says is signed in now, whatever the current owner is. */
    suspend fun signedInOwner(): String = authRepository.getCurrentUser().getOrNull()?.id ?: OwnerIds.GUEST

    fun switchTo(ownerId: String) {
        current.value = ownerId
    }
}
