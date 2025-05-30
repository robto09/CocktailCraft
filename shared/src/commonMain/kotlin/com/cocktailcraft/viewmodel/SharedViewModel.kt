package com.cocktailcraft.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.core.component.KoinComponent

/**
 * Base ViewModel class for Kotlin Multiplatform.
 * Provides a coroutine scope and lifecycle management.
 */
abstract class SharedViewModel : KoinComponent {
    
    /**
     * Coroutine scope for the ViewModel.
     * Uses Main dispatcher with a SupervisorJob to handle failures gracefully.
     */
    protected val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    /**
     * Called when the ViewModel is cleared.
     * Cancels the coroutine scope to prevent memory leaks.
     */
    open fun onCleared() {
        viewModelScope.cancel()
    }
}