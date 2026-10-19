# CocktailCraft Documentation Index

Technical documentation for CocktailCraft, a Kotlin Multiplatform cocktail ordering and discovery app for Android and iOS. Start with the [root README](../README.md) for the overview, module structure, features, setup, [testing](../README.md#testing) and [localization](../README.md#localization); the pages below cover individual subsystems.

## Architecture

- **[Shared ViewModel Strategy](Shared_ViewModel_Strategy.md)**: the 9 shared KMP ViewModels and their `SharedViewModel` base, the state/error/events contract, the iOS SKIE wrappers, direct consumption on Android, and the SKIE configuration
- **[Dependency Injection](DependencyInjection.md)**: Koin modules per Gradle module, the data layer (repository conventions and storage), ViewModel wiring, and DI testing
- **Diagrams** (mermaid): [Catalog flow](images/mermaid/catalog_flow.md) · [Place order sequence](images/mermaid/sequence_diagram_place_order.md)

## Features & UI

- **[Advanced Search](AdvancedSearch.md)**: multi-criteria search within TheCocktailDB free-tier limits, and the related-cocktails row on the detail screen
- **[UI Components](UI_Components.md)**: reusable Android Compose components, theming and animations
- **[iOS UI Components](iOS_UI_Components.md)**: reusable SwiftUI components

## Build & Tooling

- **[Libraries](Libraries.md)**: every dependency with its version and purpose (`libraries.toml`)
- **[iOS Setup Instructions](../iosApp/iOS_Setup_Instructions.md)**: iOS development environment setup
- **[DebugSwift](DebugSwift.md)**: the Debug-only iOS in-app debugging tool and how it is kept out of Release builds
