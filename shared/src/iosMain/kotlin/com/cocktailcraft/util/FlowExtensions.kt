package com.cocktailcraft.util

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow

/**
 * Extension function to collect Flow from iOS with proper error handling
 */
fun <T> Flow<T>.collect(
    collector: FlowCollector<T>,
    completion: (Throwable?) -> Unit
): DisposableHandle {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val job = scope.launch {
        try {
            collect(collector)
            completion(null)
        } catch (e: Throwable) {
            completion(e)
        }
    }
    
    return DisposableHandle { 
        job.cancel()
        scope.cancel()
    }
}

/**
 * Create a DisposableHandle that can be used from iOS
 */
class DisposableHandle(private val onDispose: () -> Unit) : kotlinx.coroutines.DisposableHandle {
    override fun dispose() {
        onDispose()
    }
}