# CocktailCraft Documentation Index

This directory contains the technical documentation for CocktailCraft, a Kotlin Multiplatform cocktail ordering and discovery app for Android and iOS. For the project overview, architecture summary, features, and setup instructions, start with the [root README](../README.md). The pages below cover individual subsystems in depth.

## Architecture & Patterns

- **[Shared ViewModel Strategy](Shared_ViewModel_Strategy.md)**: ViewModel architecture — the 8 shared KMP ViewModels plus the `SharedViewModel` base, the 8 iOS SKIE wrappers, direct consumption on Android, and the SKIE configuration
- **[Dependency Injection](DependencyInjection.md)**: Koin module structure, ViewModel wiring, and testing approach
- **[Repository Methods](REPOSITORY_METHODS.md)**: Index of the nine focused repository interfaces and their conventions

## Features & Implementation

- **[Advanced Search](AdvancedSearch.md)**: Multi-criteria search (query, category, ingredient, alcoholic) within TheCocktailDB free-tier constraints
- **[Recommendation System](RecommendationSystem.md)**: Category-based related-cocktail suggestions and their design constraints
- **[UI Components](UI_Components.md)**: Reusable Android Compose components and the animation system
- **[iOS UI Components](iOS_UI_Components.md)**: Reusable SwiftUI components

## Build, Tooling & Testing

- **[Libraries](Libraries.md)**: Every dependency with version and purpose, managed via the `libraries.toml` version catalog
- **[DebugSwift](DebugSwift.md)**: iOS in-app debugging tool integration and QA usage
- **[iOS Setup Instructions](../iosApp/iOS_Setup_Instructions.md)**: Complete iOS development environment setup

## Architecture Diagrams

Mermaid diagrams under [`images/mermaid/`](images/mermaid/):

- **[Catalog Flow](images/mermaid/catalog_flow.md)**: How a category page flows from TheCocktailDB through the database to the Home screen
- **[Sequence Diagram — Place Order](images/mermaid/sequence_diagram_place_order.md)**: Order placement flow across layers
