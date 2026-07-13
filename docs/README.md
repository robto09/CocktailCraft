# CocktailCraft Documentation Index

This directory contains the technical documentation for CocktailCraft, a Kotlin Multiplatform cocktail ordering and discovery app for Android and iOS. For the project overview, architecture summary, features, and setup instructions, start with the [root README](../README.md). The pages below cover individual subsystems in depth.

## Architecture & Patterns

- **[Shared ViewModel Strategy](Shared_ViewModel_Strategy.md)**: ViewModel architecture — the 9 shared KMP ViewModels, the 8 iOS SKIE wrappers, and direct consumption on Android
- **[Dependency Injection](DependencyInjection.md)**: Koin module structure, ViewModel wiring, and testing approach
- **[Repository Methods](REPOSITORY_METHODS.md)**: Index of the nine focused repository interfaces and their conventions
- **[SKIE Full Migration Guide](SKIE_FULL_MIGRATION_GUIDE.md)**: SKIE setup and the Kotlin-to-Swift interop patterns it enables

## Features & Implementation

- **[Advanced Search](AdvancedSearch.md)**: Multi-criteria search (query, category, ingredient, alcoholic) within TheCocktailDB free-tier constraints
- **[Recommendation System](RecommendationSystem.md)**: Category-based related-cocktail suggestions and their design constraints
- **[UI Components](UI_Components.md)**: Reusable Android Compose components and the animation system
- **[iOS UI Components](iOS_UI_Components.md)**: Reusable SwiftUI components
- **[iOS Background Sync](ios-background-sync.md)**: Background data synchronization with BGTaskScheduler on iOS

## Build, Tooling & Testing

- **[Libraries](Libraries.md)**: Every dependency with version and purpose, managed via the `libraries.toml` version catalog
- **[Testing Implementation Summary](TESTING_IMPLEMENTATION_SUMMARY.md)**: Test structure and how to run the shared, Android, and iOS suites
- **[DebugSwift](DebugSwift.md)**: iOS in-app debugging tool integration and QA usage
- **[iOS Setup Instructions](../iosApp/iOS_Setup_Instructions.md)**: Complete iOS development environment setup

## Historical Records

- **[Enhancement Tracker](ENHANCEMENT_TRACKER.md)**: Historical record of the enhancement batch work items
- **[Improvement Plan](IMPROVEMENT_PLAN.md)**: Historical improvement planning document

## Architecture Diagrams

Mermaid diagrams under [`images/mermaid/`](images/mermaid/):

- **[High-Level Architecture](images/mermaid/high_level_architecture.md)**: Application layers and their relationships
- **[Component Diagram](images/mermaid/component_diagram.md)**: Main components and their interactions
- **[Use Case Diagram](images/mermaid/use_case_diagram.md)**: User interactions with the system
- **[Class Diagram](images/mermaid/class_diagram.md)**: Domain models and their relationships
- **[Sequence Diagram — Place Order](images/mermaid/sequence_diagram_place_order.md)**: Order placement flow across layers
- **[Data Flow Diagram](images/mermaid/data_flow_diagram.md)**: How data moves through the system
- **[Package Diagram](images/mermaid/package_diagram.md)**: Package structure of the codebase
