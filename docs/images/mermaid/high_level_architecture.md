# High-Level Architecture Diagram

```mermaid
graph TD
    subgraph "CocktailCraft Application"
        subgraph "Android App"
            UI["UI Layer<br>(Compose Screens)"]
            Theme["Theme System<br>(Light/Dark Mode)"]
            VM["ViewModels"]
            Nav["Navigation"]
            ErrorUI["Error Handling UI"]
        end

        subgraph "Shared Module"
            subgraph "Domain Layer"
                Models["Models"]
                RepoInt["Repository Interfaces<br>(9 focused repositories)"]
                UC["Use Cases"]
            end

            subgraph "Data Layer"
                RepoImpl["Repository Implementations"]
                Remote["Remote Data Source"]
                Local["Local Data Source"]
                Cache["Caching System"]
                Network["Network Monitor"]
                ErrorHandler["Error Handler"]
            end

            subgraph "Cross-Cutting Concerns"
                DI["Dependency Injection<br>(Koin)"]
                Config["App Configuration"]
            end
        end

        subgraph "Platform-Specific"
            AndroidImpl["Android Implementation"]
            iOSImpl["iOS Implementation"]
        end
    end

    %% Relationships
    UI --> VM
    UI --> Theme
    UI --> ErrorUI
    VM --> UC
    VM --> Nav
    VM --> ErrorHandler
    UC --> RepoInt
    RepoImpl --> RepoInt
    RepoImpl --> Remote
    RepoImpl --> Local
    RepoImpl --> Cache
    RepoImpl --> Network
    RepoImpl --> Models
    RepoImpl --> ErrorHandler
    AndroidImpl --> RepoImpl
    iOSImpl --> RepoImpl
    DI --> RepoImpl
    DI --> UC
    DI --> VM
    DI --> Network
    Config --> RepoImpl
```

This diagram shows the high-level architecture of the CocktailCraft application, including:

1. **Android App Layer**: UI components, Navigation, Theme System for Dark Mode support, and Error Handling UI (the ViewModels themselves live in the shared module; iOS mirrors this layer in SwiftUI)
2. **Shared Module**:
   - **Domain Layer**: Models, the nine focused Repository Interfaces, and Use Cases (related-cocktail recommendations are part of `GetCocktailDetailUseCase`)
   - **Data Layer**: Repository Implementations, Remote/Local Data Sources, Caching System for Offline Mode, Network Monitor, and Error Handler
   - **Cross-Cutting Concerns**: Dependency Injection with Koin and App Configuration
3. **Platform-Specific Implementations**: Android and iOS implementations