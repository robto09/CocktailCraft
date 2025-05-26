# CocktailCraft Libraries

This document provides detailed information about the libraries used in the CocktailCraft application.

## Detailed Libraries Table

| Library | Version | Purpose |
|---------|---------|---------|
| **Core & Architecture** |  |  |
| Kotlin | 1.9.22 | Programming language for cross-platform development |
| Kotlin Coroutines | 1.7.3 | Asynchronous programming framework |
| Koin | 3.4.0 | Modular dependency injection framework |
| Ktor | 2.0.0 | HTTP client for API communication |
| Kotlinx Serialization | 1.6.0 | JSON/data serialization |
| Multiplatform Settings | 1.1.1 | Cross-platform settings/preferences storage |
| Kotlinx DateTime | 0.5.0 | Date and time handling |
| **UI & Navigation** |  |  |
| Jetpack Compose | 1.0.5 | Modern declarative UI toolkit |
| Compose Material3 | 1.0.0-alpha01 | Material Design 3 implementation for Compose |
| Compose BOM | 2023.01.00 | Bill of Materials for Compose dependencies |
| Activity Compose | 1.8.2 | Integration between Compose and Activities |
| Navigation Compose | 2.7.7 | Navigation framework for Compose |
| Accompanist | 0.30.0 | Utilities for Jetpack Compose (System UI Controller, Navigation Animation) |
| Coil | 2.4.0 | Image loading library for Android |
| Kamel | 0.3.0 | Multiplatform image loading library |
| **State Management** |  |  |
| Lifecycle ViewModel | 2.7.0 | Component to store and manage UI-related data |
| DataStore | 1.0.0 | Data storage solution (replaces SharedPreferences) |
| **Security** |  |  |
| Security Crypto | 1.1.0-alpha03 | Encryption and security utilities |
| **Testing** |  |  |
| JUnit | 4.13.2 | Unit testing framework |
| Mockito | 5.3.1 | Mocking framework for unit tests |
| Mockito Kotlin | 5.1.0 | Kotlin extensions for Mockito |
| Mockk | 1.13.8 | Kotlin-friendly mocking library |
| Turbine | 0.12.1 | Testing library for Kotlin Flow |
| Espresso | 3.5.0 | UI testing framework for Android |
| Navigation Testing | 2.7.7 | Testing utilities for Navigation component |
| **Dependency Injection** |  |  |
| Koin | 3.4.0 | Modular dependency injection framework with improved testability |
| Koin Test | 3.4.0 | Testing utilities for Koin dependency injection |

## Important Notes

### Compose Compiler Compatibility
The project uses Compose Compiler Extension version 1.5.8, which is compatible with Kotlin 1.9.22. When updating Kotlin versions, ensure the Compose Compiler version is also updated according to the [official compatibility table](https://developer.android.com/jetpack/androidx/releases/compose-kotlin).

## Library Categories

### Core & Architecture
These libraries form the foundation of the application architecture, providing essential functionality for cross-platform development, asynchronous programming, and data handling.

### UI & Navigation
These libraries are responsible for the user interface, including the modern declarative UI toolkit Jetpack Compose, navigation between screens, and image loading.

### State Management
These libraries help manage application state, including UI-related data and persistent storage.

### Security
Security libraries provide encryption and other security utilities to protect sensitive data.

### Testing
These libraries support various testing approaches, including unit testing, mocking, and UI testing.

### Dependency Injection
Koin provides a lightweight and pragmatic dependency injection framework that helps maintain clean architecture and testability.
