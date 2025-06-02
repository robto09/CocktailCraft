package com.cocktailcraft.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import org.koin.core.component.KoinComponent

/**
 * Base ViewModel class for Kotlin Multiplatform.
 * Now extends kmp-viewmodel's ViewModel which provides:
 * - Automatic coroutine scope management (viewModelScope)
 * - Lifecycle management (onCleared is handled automatically)
 * - Platform-specific optimizations
 */
abstract class SharedViewModel : ViewModel(), KoinComponent