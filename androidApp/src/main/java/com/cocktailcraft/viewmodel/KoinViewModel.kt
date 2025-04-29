package com.cocktailcraft.viewmodel

import androidx.lifecycle.ViewModel
import org.koin.core.component.KoinComponent

/**
 * Base ViewModel class that implements KoinComponent.
 * All ViewModels that need Koin injection should extend this class.
 *
 * This provides a consistent pattern for dependency injection in ViewModels.
 *
 * In the MVVM + Clean Architecture approach:
 * - ViewModels should primarily depend on use cases, not repositories
 * - Dependencies should be injected via constructor parameters when possible
 * - This class provides KoinComponent functionality for cases where constructor injection is not feasible
 */
abstract class KoinViewModel : ViewModel(), KoinComponent
