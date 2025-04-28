package com.cocktailcraft.viewmodel

import androidx.lifecycle.ViewModel
import org.koin.core.component.KoinComponent

/**
 * Base ViewModel class that implements KoinComponent.
 * All ViewModels that need Koin injection should extend this class.
 * 
 * This provides a consistent pattern for dependency injection in ViewModels.
 */
abstract class KoinViewModel : ViewModel(), KoinComponent
