# CocktailCraft Architecture Diagrams

This directory contains various architecture diagrams for our CocktailCraft application. These diagrams provide a visual representation of different aspects of our system's architecture.

## Available Diagrams

### 1. [High-Level Architecture Diagram](high_level_architecture.md)

I've created this diagram to provide an overview of our application's architecture, showing the main layers and their relationships:
- Android App (UI Layer, ViewModels, Navigation)
- Shared Module (Domain Layer, Data Layer)
- Platform-Specific Implementations

### 2. [Component Diagram](component_diagram.md)

I've designed this diagram to show the main components of our application and how they interact with each other:
- UI Components (Screens, UI Elements)
- ViewModels
- Domain Layer (Repositories, Use Cases, Models)
- Data Layer (Repository Implementations, API)
- Dependency Injection

### 3. [Use Case Diagram](use_case_diagram.md)

I've illustrated the main user interactions with our system:
- Browse and search cocktails
- Cart management
- Favorites management
- Order management
- Profile management
- Review management

### 4. [Class Diagram](class_diagram.md)

I've created this diagram to show the key domain models and their relationships:
- Domain Models (Cocktail, Order, User, etc.)
- Repository Interfaces
- Use Cases

### 5. [Sequence Diagram - Place Order](sequence_diagram_place_order.md)

I've designed this diagram to illustrate the sequence of interactions when a user places an order:
- User interaction with UI
- ViewModel processing
- Use Case execution
- Repository operations
- Data storage

### 6. [Data Flow Diagram](data_flow_diagram.md)

I've created this diagram to show how data flows through our application:
- User input and UI updates
- Data flow between layers
- External API interactions
- Local storage operations

### 7. [Package Diagram](package_diagram.md)

I've designed this diagram to show the organization of our codebase:
- Package structure
- Dependencies between packages
- Module organization


## Architecture Overview

CocktailCraft follows Clean Architecture principles with a MVVM pattern for the UI layer:

1. **UI Layer**:
   - Jetpack Compose UI components
   - Screen composables
   - Navigation

2. **ViewModel Layer**:
   - Manages UI state
   - Handles user actions
   - Communicates with domain layer

3. **Domain Layer**:
   - Business logic
   - Use cases
   - Repository interfaces
   - Domain models

4. **Data Layer**:
   - Repository implementations
   - Remote data sources (API)
   - Local data sources (Storage)
   - DTOs and mappers

5. **Platform-Specific**:
   - Android-specific implementations
   - iOS-specific implementations

