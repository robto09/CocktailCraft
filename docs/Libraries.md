# CocktailCraft Libraries

This document provides detailed information about the libraries used in the CocktailCraft application.

## Detailed Libraries Table

| Library | Version | Purpose |
|---------|---------|---------|
| **Core & Architecture** |  |  |
| Kotlin | 2.0.21 | Programming language for cross-platform development |
| Kotlin Coroutines | 1.10.1 | Asynchronous programming framework |
| Koin | 4.0.1 | Modular dependency injection framework |
| Ktor | 3.0.3 | HTTP client for API communication |
| Kotlinx Serialization | 1.7.3 | JSON/data serialization |
| Multiplatform Settings | 1.1.1 | Cross-platform settings/preferences storage |
| Kotlinx DateTime | 0.6.0 | Date and time handling |
| **UI & Navigation** |  |  |
| Jetpack Compose | 1.2.0-alpha01-dev709 | Modern declarative UI toolkit |
| Compose Material3 | 1.3.1 | Material Design 3 implementation for Compose |
| Compose BOM | 2025.02.00 | Bill of Materials for Compose dependencies |
| Activity Compose | 1.8.0 | Integration between Compose and Activities |
| Navigation Compose | 2.8.8 | Navigation framework for Compose |
| Accompanist | 0.32.0 | Utilities for Jetpack Compose |
| Coil | 2.6.0 | Image loading library for Android |
| Kamel | 1.0.3 | Multiplatform image loading library |
| **State Management** |  |  |
| Lifecycle ViewModel | 2.8.7 | Component to store and manage UI-related data |
| DataStore | 1.1.3 | Data storage solution (replaces SharedPreferences) |
| **Security** |  |  |
| Security Crypto | 1.1.0-alpha06 | Encryption and security utilities |
| **Testing** |  |  |
| JUnit | 4.13.2 | Unit testing framework |
| Mockito | Various | Mocking framework for unit tests |
| Mockk | 1.13.8 | Kotlin-friendly mocking library |
| Turbine | 1.0.0 | Testing library for Kotlin Flow |
| Espresso | 3.6.1 | UI testing framework for Android |
| **Dependency Injection** |  |  |
| Koin | 4.0.1 | Modular dependency injection framework with improved testability |
| Koin Test | 4.0.1 | Testing utilities for Koin dependency injection |

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
